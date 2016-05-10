package book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import exceptions.InvalidVolumeOperation;
import exceptions.OrderNotFoundException;
import messages.CancelMessage;
import messages.FillMessage;
import pricePackage.Price;
import pricePackage.PriceFactory;
import publishers.CurrentMarketPublisher;
import publishers.LastSalePublisher;
import publishers.MarketDataDTO;
import publishers.MessagePublisher;
import tradablePackage.Order;
import tradablePackage.Quote;
import tradablePackage.Tradable;
import tradablePackage.TradableDTO;

public class ProductBook {

	private String product;
	private ProductBookSide buySide;
	private ProductBookSide sellSide;
	private String marketDataValue = "";
	private HashSet<String> userQuotes = new HashSet<>();
	private HashMap<Price, ArrayList<Tradable>> oldEntries = 
													new HashMap<>();
	
	public ProductBook(String p){
		this.setProduct(p);
		buySide = new ProductBookSide(this, "BUY");
		sellSide = new ProductBookSide(this, "SELL");
	}
	
	// set methods
	private void setProduct(String p){
		if(p == null){
			throw new NullPointerException("Product input parameter cannot be null");
		}
		product = p;
	}
	
	// get methods
	public String getProduct(){
		return product;
	}
	public String marketDataValue(){
		return marketDataValue;
	}
	
	// Query Methods
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName){
		ArrayList<TradableDTO> tradableDTOList = new ArrayList<>();
		
		ArrayList<TradableDTO> buySideDTOList = buySide.getOrdersWithRemaining(userName);
		tradableDTOList.addAll(buySideDTOList);
		
		ArrayList<TradableDTO> sellSideDTOList = sellSide.getOrdersWithRemaining(userName);
		tradableDTOList.addAll(sellSideDTOList);
		
		return tradableDTOList;
	}
	
	public synchronized void checkTooLateToCancel(String orderId) throws OrderNotFoundException{
		boolean flag = false;
		
		for(Price price : oldEntries.keySet()){
			for(Tradable tradable : oldEntries.get(price)){
				if(tradable.getId().equals(orderId)){
					
					CancelMessage cancelMessage = new CancelMessage(tradable.getUser(), tradable.getProduct(),
							tradable.getPrice(), tradable.getRemainingVolume(),
							"Too Late to Cancel", tradable.getSide(),
							tradable.getId());
					
					MessagePublisher.getInstance().publishCancel(cancelMessage);
					flag = true;
				}
			}
		}
		if(flag == false){
			throw new OrderNotFoundException("Order not found");
		}
	}
	
	public synchronized String[][] getBookDepth(){
		String[][] bd = new String[2][];
		bd[0] = buySide.getBookDepth();
		bd[1] = sellSide.getBookDepth();
		return bd;
	}
	
	public synchronized MarketDataDTO getMarketData(){
		Price bestBuyPrice = buySide.topOfBookPrice();
		Price bestSellPrice = sellSide.topOfBookPrice();
		
		if(bestBuyPrice == null){
			bestBuyPrice = PriceFactory.makeLimitPrice("0"); //POSSIBLE ERROR DUE TO MKT PRICE
		}
		if(bestSellPrice == null){
			bestSellPrice = PriceFactory.makeLimitPrice("0"); // ^^
		}
		
		int bestBuyVolume = buySide.topOfBookVolume();
		int bestSellVolume = sellSide.topOfBookVolume();
		
		return new MarketDataDTO(this.product, bestBuyPrice, bestBuyVolume,
								 bestSellPrice, bestSellVolume);
		
	}
	

	public synchronized void addOldEntry(Tradable t) throws InvalidVolumeOperation{
		if(!oldEntries.containsKey(t.getPrice())){
			oldEntries.put(t.getPrice(), new ArrayList<Tradable>());
		}
	    
		int x = t.getRemainingVolume();
	    t.setRemainingVolume(0);
	    t.setCancelledVolume(x);
		
		//t.setCancelledVolume(t.getRemainingVolume());
		//t.setRemainingVolume(0);
		oldEntries.get(t.getPrice()).add(t);
	}
	
	//This method "opens" the book for trading. Any resting Order and QuoteSides
	//that are immediately tradable upon opening should be traded.
	public synchronized void openMarket() throws InvalidVolumeOperation{
		
		Price buyPrice = buySide.topOfBookPrice();
		Price sellPrice = sellSide.topOfBookPrice();
		
		// If one of these sides has no Tradables then there will 
		// be no opening trade.
		if(buyPrice == null || sellPrice == null){
			return;
		}
		
		while(buyPrice.greaterOrEqual(sellPrice) || buyPrice.isMarket() || sellPrice.isMarket()){
			
			//Create new list to hold entries at top of buy-side book
			ArrayList<Tradable> topOfBuySide = buySide.getEntriesAtPrice(buyPrice);
			
			//Create new hashMap to hold fillMessages
			HashMap<String, FillMessage> allFills = null;
			
			//Create new List to hold entries that fully trade as result of processing.
			//These are the tradables that will be removed from the book when opening
			//trade is complete
			ArrayList<Tradable> toRemove = new ArrayList<>();
			
			//Tries to trade every Tradable in topOfBuySide. If all Tradables
			//are traded, then the Tradable gets added to the toRemove list.
			for(Tradable t : topOfBuySide){
				allFills = sellSide.tryTrade(t);
				if(t.getRemainingVolume() == 0){
					toRemove.add(t);
				}
			}
			
			//Removes all Tradables in toRemove from the buySide ProductBookSide
			for(Tradable t : toRemove){
				buySide.removeTradable(t);
			}
			
			//Update the current market
			updateCurrentMarket();
			
			//Determine the Book's last sale price and volume
			Price lastSalePrice = determineLastSalePrice(allFills);
			int lastSaleVolume = determineLastSaleQuantity(allFills);
			
			//Publishes a Last Sale message 
			LastSalePublisher.getInstance().publishLastSale(product, lastSalePrice, lastSaleVolume);
			
			//Reset top buy and sell prices
			buyPrice = buySide.topOfBookPrice();
			sellPrice = sellSide.topOfBookPrice();
			
			//If either of these prices is null, then one of these sides has
			//no tradables, so there will be no trade.
			if(buyPrice == null || buyPrice == null){
				break;
			}
		}
	}
	
	//Closes the book for trading
	public synchronized void closeMarket() throws InvalidVolumeOperation,
												  OrderNotFoundException {
		buySide.cancelAll(); 
		sellSide.cancelAll();
		updateCurrentMarket();
	}
	
	//Cancels the Order specified by the provided orderId on the specified side
	public synchronized void cancelOrder(String side, String orderId) throws InvalidVolumeOperation,
																			 OrderNotFoundException{
		if(side.equals("BUY")){
			buySide.submitOrderCancel(orderId);			
		}
		else if(side.equals("SELL")){
			sellSide.submitOrderCancel(orderId);
		}
		else{
			throw new OrderNotFoundException("The order you want to cancel has not been found");
		}
	}
	
	//Cancels the specified user's Quote on both the BUY and SELL sides
	public synchronized void cancelQuote(String userName){
		buySide.submitQuoteCancel(userName);
		sellSide.submitQuoteCancel(userName);
		updateCurrentMarket();
	}
	
	//Adds the provided Quote's sides to the Buy and Sell ProductSideBooks
	public synchronized void addToBook(Quote q) throws InvalidVolumeOperation{
		
		//If q's SELL side's Price is less than or equal to the BUY side, then throw an exception
		if(q.getQuoteSide("SELL").getPrice().lessOrEqual(q.getQuoteSide("BUY").getPrice())){
			//THROW DataValidationException
		}
		
		//If either side's Prices are less than or equal to zero, then throw an exception 
		if(q.getQuoteSide("BUY").getPrice().lessOrEqual(PriceFactory.makeLimitPrice(0)) ||
				q.getQuoteSide("SELL").getPrice().lessOrEqual(PriceFactory.makeLimitPrice(0))){
			//THROW DataValidationException
		}
		
		//If either side's original volume is less than or equal to zero, then throw and exception
		if(q.getQuoteSide("BUY").getOriginalVolume() <= 0 ||
				q.getQuoteSide("SELL").getOriginalVolume() <= 0){
			//THROW DataValidationException
		}
		
		//If the userQuotes contains the q's user, remove the quote from the book sides
		//and then updates the market
		if(userQuotes.contains(q.getUserName())){
			buySide.removeQuote(q.getUserName());
			sellSide.removeQuote(q.getUserName());
			updateCurrentMarket();
		}
		
		//Add quote sides to the Product Book
		addToBook("BUY", q.getQuoteSide("BUY"));
		addToBook("SELL", q.getQuoteSide("SELL"));
		
		//add the quote's username to the quote username list and update the market
		userQuotes.add(q.getUserName());
		updateCurrentMarket();
	}
	
	//Add the provided Order to the appropriate ProductSideBook
	//and update the market
	public synchronized void addToBook(Order o) throws InvalidVolumeOperation{
		addToBook(o.getSide(), o);
		updateCurrentMarket();
	}
	
	//Updates the market for this current stock product
	public synchronized void updateCurrentMarket(){
		
        Price bp = buySide.topOfBookPrice();
        if (bp == null) {
            bp = PriceFactory.makeLimitPrice("0.00");
        }
        Price sp = sellSide.topOfBookPrice();
        if (sp == null) {
            sp = PriceFactory.makeLimitPrice("0.00");
        }
		
        String s = bp.toString() + buySide.topOfBookVolume() + sp.toString() + sellSide.topOfBookVolume();;
        
		//Sets temporary string variable
		//String s = buySide.topOfBookPrice().toString() + buySide.topOfBookVolume() +
		//			sellSide.topOfBookPrice().toString() + sellSide.topOfBookVolume();
		
		//Checks to see if the last current market value is the same as the
		//temporary string. If not, creates a new MarketDataDTO and publishes a 
		//current market Message, then sets the marketDataValue String to 
		//the temporary string
		if(!marketDataValue.equals(s)){
			MarketDataDTO marketDataDTO = new MarketDataDTO(product, buySide.topOfBookPrice(),
															buySide.topOfBookVolume(), sellSide.topOfBookPrice(),
															sellSide.topOfBookVolume());
			CurrentMarketPublisher.getInstance().publishCurrentMarket(marketDataDTO);
			marketDataValue = s;
		}
	}
	
	//Takes a hashMap of FillMessages passed in and determines from the 
	//information it contains what the Last Sale price is
	private synchronized Price determineLastSalePrice(HashMap<String, FillMessage> fills){
		ArrayList<FillMessage> msgs = new ArrayList<>(fills.values());
		Collections.sort(msgs);
		return msgs.get(0).getPrice();
	}
	
	//Takes a hashMap of FillMessages passed in and determines from the
	//information it contains what the Last Sale quantity is
	private synchronized int determineLastSaleQuantity(HashMap<String, FillMessage> fills){
		ArrayList<FillMessage> msgs = new ArrayList<>(fills.values());
		Collections.sort(msgs);
		return msgs.get(0).getVolume();
	}
	
	//Deals with the addition of Tradables to the Buy/Sell ProductSideBook
	//and handles the results of any trades that result from that addition
	private synchronized void addToBook(String side, Tradable trd) throws InvalidVolumeOperation{
		
		//Will add Tradable to books if market state is PREOPEN
		if(ProductService.getInstance().getMarketState().equals("PREOPEN")){
			if(side.equals("BUY")){
				buySide.addToBook(trd);
				return;
			}
			if(side.equals("SELL")){
				sellSide.addToBook(trd);
				return;
			}
		}
		//temporary HashMap to collect potential Fill Messages
		HashMap<String, FillMessage> allFills = null;
		
		//Tries the trade
		if(side.equals("BUY")){
			allFills = sellSide.tryTrade(trd);
		}
		if(side.equals("SELL")){
			allFills = buySide.tryTrade(trd);
		}
		
		/*
		 * If the allFills Hashmap is not null or empty, update the market
		 * and publish a message with the last sale and remaining volume
		 */
		if(!allFills.isEmpty() && !(allFills == null)){
			updateCurrentMarket();
			int dif = trd.getOriginalVolume() - trd.getRemainingVolume();
			Price lastSalePrice = determineLastSalePrice(allFills);
			LastSalePublisher.getInstance().publishLastSale(side, lastSalePrice, dif);
		}
		
		//If the Tradable's volume is greater than zero...
		if(trd.getRemainingVolume() > 0){
			//Check to see if the tradable is a MKT price. If so, Cancel it, because it will be traded automatically. 
			if(trd.getPrice().isMarket()){
				MessagePublisher.getInstance().publishCancel(new CancelMessage(trd.getUser(), trd.getProduct(),
															 					trd.getPrice(), trd.getRemainingVolume(),
															 					"Cancelled", trd.getSide(), trd.getId()));
			}
			else{
				if(side.equals("BUY")){
					buySide.addToBook(trd);
				}
				if(side.equals("SELL")){
					sellSide.addToBook(trd);
				}
			}
		}
	}
}
