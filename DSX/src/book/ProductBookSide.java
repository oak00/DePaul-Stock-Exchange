package book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import exceptions.InvalidVolumeOperation;
import exceptions.OrderNotFoundException;
import messages.CancelMessage;
import messages.FillMessage;
import pricePackage.Price;
import publishers.MessagePublisher;
import tradablePackage.Order;
import tradablePackage.Tradable;
import tradablePackage.TradableDTO;



public class ProductBookSide {
	
	private String side;
	private HashMap<Price, ArrayList<Tradable>> bookEntries = 
													new HashMap<>();
	private TradeProcessor tradeProcessor;
	private ProductBook productBook;
	
	public ProductBookSide(ProductBook pb, String s){
		this.setProductBook(pb);
		this.setSide(s);
		this.setTradeProcessor();
	}
	
	// set methods
	private void setSide(String s){ 
		if(s == null){
			throw new NullPointerException("Side parameter cannot be null");
		}
		side = s;
	}
	private void setTradeProcessor(){
		tradeProcessor = new TradeProcessorPriceTimeImpl(this);
	}
	private void setProductBook(ProductBook pb){
		if(pb == null){
			throw new NullPointerException("Side parameter cannot be null");
		}
		productBook = pb;
	}
	
	// get methods, throw exceptions for all of them
	public String getSide(){
		return side;
	}
	
	// ProductBookSide Query Methods
	public synchronized ArrayList<TradableDTO> getOrdersWithRemaining(String userName){
		
		ArrayList<TradableDTO> tradableDTOList =  new ArrayList<>();
		
		for(Price price : bookEntries.keySet()){
			ArrayList<Tradable> orders = bookEntries.get(price);
			
			for(Tradable order: orders){
				if(order.getUser().equals(userName) && order.getRemainingVolume() > 0){
					TradableDTO orderInfo = new TradableDTO(order.getProduct(), order.getPrice(),
															order.getOriginalVolume(), order.getRemainingVolume(),
															order.getCancelledVolume(), order.getUser(),
															order.getSide(), order.isQuote(), order.getId());
					tradableDTOList.add(orderInfo);
				}
			}
		}
		return tradableDTOList;
	}
	
	synchronized ArrayList<Tradable> getEntriesAtTopOfBook(){
		
		if(bookEntries == null){
			return null;
		}
		
		ArrayList<Price> sorted = new ArrayList<>(bookEntries.keySet());
		Collections.sort(sorted);
		if(side.equals("BUY")){
			Collections.reverse(sorted);
		}
		return bookEntries.get(sorted.get(0));
		
	}
	
	public synchronized String[] getBookDepth(){
		
		if(bookEntries.isEmpty()){
			String[] emptyBook = new String[1];
			emptyBook[0] = "<Empty>";
			return emptyBook;
		}
		
		String[] totalPriceVolume = new String[bookEntries.size()];
		
		ArrayList<Price> sorted = new ArrayList<>(bookEntries.keySet());
		Collections.sort(sorted);
		if(side.equals("BUY")){
			Collections.reverse(sorted);
		}

		int sumVolume = 0;
		int i = 0;
		
		for(Price price : sorted){
			ArrayList<Tradable> TradableList = bookEntries.get(price);
			for(Tradable tradable : TradableList){
				sumVolume += tradable.getRemainingVolume();
			}
			String priceDepth = price + " x " + sumVolume;
			totalPriceVolume[i] = priceDepth;
			i++;
			sumVolume = 0; //RESET 
		}
		return totalPriceVolume;
	}
	
	synchronized ArrayList<Tradable> getEntriesAtPrice(Price price){
		if(!bookEntries.containsKey(price)){
			return null;
		}
		return bookEntries.get(price);
	}
	
	public synchronized boolean hasMarketPrice(){ // POTENTIAL HUGE ERROR DUE TO MKT
		if(bookEntries.containsKey("MKT")){
			return true;
		}
		return false;
	}
	
	public synchronized boolean hasOnlyMarketPrice(){
		if(bookEntries.size() == 1 && bookEntries.containsKey("MKT")){ //POTENTIAL HUGE ERROR DUE TO MKT
			return true;
		}
		return false;
	}
	
	public synchronized Price topOfBookPrice(){
		if(bookEntries.isEmpty()){
			return null;
		}
		
		ArrayList<Price> sorted = new ArrayList<>(bookEntries.keySet());
		Collections.sort(sorted);
		if(side.equals("BUY")){
			Collections.reverse(sorted);
		}
		return sorted.get(0);
		
	}
	
	public synchronized int topOfBookVolume(){
		if(bookEntries.isEmpty()){
			return 0;
		}
		
		ArrayList<Price> sorted = new ArrayList<>(bookEntries.keySet());
		Collections.sort(sorted);
		if(side.equals("BUY")){
			Collections.reverse(sorted);
		}
		
		int sumVolume = 0;
		Price p = sorted.get(0);
		
		ArrayList<Tradable> TradableList = bookEntries.get(p);
		for(Tradable tradable : TradableList){
			sumVolume += tradable.getRemainingVolume();
		}
		return sumVolume;
	}
	
	public synchronized boolean isEmpty(){
		if(bookEntries.isEmpty()){
			return true;
		}
		return false;
	}
	
	//ProductBookSideManipulationMethods
	public synchronized void cancelAll() throws InvalidVolumeOperation,
												OrderNotFoundException{
		for(Price price : bookEntries.keySet()){
			for(Tradable tradable : bookEntries.get(price)){
				if(tradable.isQuote()){
					submitQuoteCancel(tradable.getUser());
				}
				submitOrderCancel(tradable.getId()); // POSSIBLE ERROR AS OF WRONG
													 //	VALUE BEING PASSED IN
			}
		}
	}
	
	public synchronized TradableDTO removeQuote(String user){
		TradableDTO tradableDTO = null;
		
		for(Price price : bookEntries.keySet()){
			for(Tradable tradable : bookEntries.get(price)){
				if(tradable.getUser().equals(user)){
					tradableDTO = new TradableDTO(tradable.getProduct(),tradable.getPrice(),
																tradable.getOriginalVolume(),tradable.getRemainingVolume(),
																tradable.getCancelledVolume(), tradable.getUser(), tradable.getSide(),
																tradable.isQuote(),tradable.getId());
					bookEntries.get(price).remove(tradable);
					if(bookEntries.get(price).isEmpty()){
						bookEntries.remove(price);
					}
					return tradableDTO;
				}
			}
		}
		return tradableDTO;
	}
	
	public synchronized void submitOrderCancel(String orderId) throws InvalidVolumeOperation,
																	  OrderNotFoundException{
		boolean flag = false;
		
		for(Price price : bookEntries.keySet()){
			for(Tradable tradable : bookEntries.get(price)){
				if(tradable.getId().equals(orderId)){
					flag = true;
					bookEntries.get(price).remove(tradable);
					
					CancelMessage cancelMessage = new CancelMessage(tradable.getUser(), tradable.getProduct(),
																	tradable.getPrice(), tradable.getRemainingVolume(),
																	"Order " + orderId + "Cancelled", tradable.getSide(),
																	tradable.getId());
					MessagePublisher.getInstance().publishCancel(cancelMessage);
					
					addOldEntry(tradable);
					
					if(bookEntries.get(price).isEmpty()){
						bookEntries.remove(price);
					}
					return;
				}
			}
		}
		if(flag == false){
			productBook.checkTooLateToCancel(orderId);
			return;
		}
	}
	
	public synchronized void submitQuoteCancel(String userName){
		TradableDTO tradableDTO = removeQuote(userName);
		
		if(tradableDTO == null){
			return;
		}
		
		CancelMessage cancelMessage = new CancelMessage(tradableDTO.user, tradableDTO.product,
														tradableDTO.price, tradableDTO.remainingVolume,
														"Quote " + side + "-Side Cancelled", tradableDTO.bookSide,
														tradableDTO.id);
		MessagePublisher.getInstance().publishCancel(cancelMessage);
	}
	
	public void addOldEntry(Tradable t) throws InvalidVolumeOperation{
		productBook.addOldEntry(t);
	}
	
	public synchronized void addToBook(Tradable trd){
		if(!bookEntries.containsKey(trd.getPrice())){
			ArrayList<Tradable> tradableList = new ArrayList<>();
			bookEntries.put(trd.getPrice(), tradableList);
		}
		bookEntries.get(trd.getPrice()).add(trd);
	}
	
	public HashMap<String, FillMessage> tryTrade(Tradable trd) throws InvalidVolumeOperation{
		HashMap<String, FillMessage> allFills = new HashMap<>();
		if(side.equals("BUY")){
			allFills = trySellAgainstBuySideTrade(trd);
		}
		else if(side.equals("SELL")){
			allFills = tryBuyAgainstSellSideTrade(trd);
		}
		for(String s : allFills.keySet()){
			MessagePublisher.getInstance().publishFill(allFills.get(s));
		}
		return allFills;
	}
	
	public synchronized HashMap<String, FillMessage> trySellAgainstBuySideTrade(Tradable trd) throws InvalidVolumeOperation{
		HashMap<String, FillMessage> allFills = new HashMap<>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<>();
		
		while((trd.getRemainingVolume() > 0 && !(bookEntries.isEmpty()) && trd.getPrice().lessOrEqual(topOfBookPrice())) ||
				trd.getRemainingVolume() > 0 && !bookEntries.isEmpty() && trd.getPrice().isMarket()){
			HashMap<String, FillMessage> someMsgs = tradeProcessor.doTrade(trd);
			fillMsgs = mergeFills(fillMsgs, someMsgs);
		}
		allFills.putAll(fillMsgs);
		return allFills;
	}
	
	private HashMap<String, FillMessage> mergeFills(HashMap<String, FillMessage> existing, HashMap<String,
			FillMessage> newOnes){
		
		if(existing.isEmpty()){
			return new HashMap<String, FillMessage>(newOnes);
		}
		
		HashMap<String, FillMessage> results = new HashMap<>(existing);
		for(String key : newOnes.keySet()){ 			// For each Trade Id key in the “newOnes” HashMap
			if(!existing.containsKey(key)){ 			// If the “existing” HashMap does not have that key…
				results.put(key, newOnes.get(key)); 	// …then simply add this entry to the “results” HashMap
			} else { 									// Otherwise, the “existing” HashMap does have that key – we need to update the data
				FillMessage fm = results.get(key); 		// Get the FillMessage from the “results” HashMap
				fm.setVolume(newOnes.get(key).getVolume()); 	// Update the fill volume
				fm.setDetails(newOnes.get(key).getDetails()); 	// Update the fill details
			}
		}
		return results;
	}
	
	public synchronized HashMap<String, FillMessage> tryBuyAgainstSellSideTrade(Tradable trd) throws InvalidVolumeOperation{
		HashMap<String, FillMessage> allFills = new HashMap<>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<>();
		
		while(trd.getRemainingVolume() > 0 && !(bookEntries.isEmpty()) && trd.getPrice().greaterOrEqual(topOfBookPrice()) ||
				trd.getRemainingVolume() > 0 && !bookEntries.isEmpty() && trd.getPrice().isMarket()){
			HashMap<String, FillMessage> someMsgs = tradeProcessor.doTrade(trd);
			fillMsgs = mergeFills(fillMsgs, someMsgs);
		}
		allFills.putAll(fillMsgs);
		return allFills;
	}
	
	public synchronized void clearIfEmpty(Price p){
		if(bookEntries.get(p).isEmpty()){
			bookEntries.remove(p);
		}
	}
	
	
	public synchronized void removeTradable(Tradable t){
		ArrayList<Tradable> entries = bookEntries.get(t.getPrice());
		if(entries == null){
			return;
		}
		boolean b = entries.remove(t);
		if( b == false){
			return;
		}
		if(entries.isEmpty()){
			clearIfEmpty(t.getPrice());
		}
	}
}
