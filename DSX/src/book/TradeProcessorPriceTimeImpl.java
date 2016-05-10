package book;

import java.util.ArrayList;
import java.util.HashMap;

import exceptions.InvalidVolumeOperation;
import pricePackage.Price;
import pricePackage.PriceFactory;
import tradablePackage.Tradable;
import messages.FillMessage;

public class TradeProcessorPriceTimeImpl implements TradeProcessor {

	private HashMap<String, FillMessage> fillMessages = new HashMap<>();
	private ProductBookSide productBookSide;
	
	public TradeProcessorPriceTimeImpl(ProductBookSide pbs){
		this.setProductBookSide(pbs);
	}
	
	//Set Method
	private void setProductBookSide(ProductBookSide p){ //THROW EXCEPTION
		productBookSide = p;
	}
	
	//Utility Methods
	
	/*
	 * Will be used when executing a trade. All Trades results
	 * in Fill Messages, which need a Trade Key so the system
	 * can tell them apart.
	 */
	private String makeFillKey(FillMessage fm){
		return fm.getUser() + fm.getId() + fm.getPrice();
	}
	
	/*
	 * Checks the contents of fillMessages to see if the FillMEssage
	 * passed in is a fillMessage for an existing known trade or if its
	 * for a new previously unrecorded trade.
	 */
	private boolean isNewFill(FillMessage fm){
		String key = makeFillKey(fm);
		if(!fillMessages.containsKey(key)){
			return true;
		}
		FillMessage oldFill = fillMessages.get(key);
		if(!oldFill.getSide().equals(fm.getSide())){
			return true;
		}
		if(!oldFill.getId().equals(fm.getId())){
			return true;
		}
		return false;
	}
	
	/*
	 * Adds a FillMessage to the fillMessages HashMap it its a new trade,
	 * or should update an existing fill message if its another
	 * part of an existing trade 
	 */
	private void addFillMessage(FillMessage fm){
		if(isNewFill(fm) == true){
			fillMessages.put(makeFillKey(fm), fm);
		}
		else{
			FillMessage temp = fillMessages.get(makeFillKey(fm));
			temp.setVolume(temp.getVolume() + fm.getVolume());
			temp.setDetails(fm.getDetails());
		}
	}
	
	
	//Interface "doTrade" method
	
	/*
	 * This method will be called when it has been determined
	 * that a Tradable can trade against the content of the book.
	 * (non-Javadoc)
	 * @see book.TradeProcessor#doTrade(tradablePackage.Tradable)
	 */
	public HashMap<String, FillMessage> doTrade(Tradable trd) throws InvalidVolumeOperation{
		//Reset the fillMessages to a new HashMap
		fillMessages = new HashMap<String, FillMessage>();
		
		//Create a new temporary ArrayList to hold the Tradables that are completely traded during this trade.
		ArrayList<Tradable> tradedOut = new ArrayList<>();
		
		//Create a new temporary ArrayList to store the top entries of each book
		ArrayList<Tradable> entriesAtPrice = productBookSide.getEntriesAtTopOfBook();
		
		for(Tradable t : entriesAtPrice){
			if(trd.getRemainingVolume() == 0){
				//For each tradable in tradedOut<>, remove it from entriesAtPrice<>
				for(Tradable t2 : tradedOut){
					entriesAtPrice.remove(t2);
				}
				if(entriesAtPrice.isEmpty()){
					productBookSide.clearIfEmpty(productBookSide.topOfBookPrice());
				}
				return fillMessages;
			}
			else if(trd.getRemainingVolume() >= t.getRemainingVolume()){
				tradedOut.add(t);
				Price tPrice = PriceFactory.makeLimitPrice(0);
				if(t.getPrice().isMarket()){
					tPrice = trd.getPrice();
				}
				else{
					tPrice = t.getPrice();
				}
				FillMessage tFM = new FillMessage(t.getUser(), t.getProduct(),
												 tPrice, t.getRemainingVolume(),
												 "leaving 0", t.getSide(), t.getId());
				addFillMessage(tFM);
				FillMessage trdFM = new FillMessage(trd.getUser(), trd.getProduct(),
													tPrice, trd.getRemainingVolume(),
													"leaving " + (trd.getRemainingVolume() - t.getRemainingVolume()),
													trd.getSide(), trd.getId());
				addFillMessage(trdFM);
				trd.setRemainingVolume(trd.getRemainingVolume() - t.getRemainingVolume());
				t.setRemainingVolume(0);
				productBookSide.addOldEntry(t);
				
				//BREAK
				//For each tradable in tradedOut<>, remove it from entriesAtPrice<>

			}
			else{
				int remainder = t.getRemainingVolume() - trd.getRemainingVolume();
				Price tPrice = PriceFactory.makeLimitPrice(0);
				if(t.getPrice().isMarket()){
					tPrice = trd.getPrice();
				}
				else{
					tPrice = t.getPrice();
				}
				FillMessage tFM = new FillMessage(t.getUser(), t.getProduct(),
												  tPrice, trd.getRemainingVolume(),
												  "leaving " + remainder, t.getSide(),
												  t.getId());
				addFillMessage(tFM);
				FillMessage trdFM = new FillMessage(trd.getUser(), trd.getProduct(),
													tPrice, trd.getRemainingVolume(),
													"leaving 0", trd.getSide(), trd.getId());
				addFillMessage(trdFM);
				trd.setRemainingVolume(0);
				t.setRemainingVolume(remainder);
				productBookSide.addOldEntry(trd);
				
				for(Tradable t2 : tradedOut){
					entriesAtPrice.remove(t2);
				}
				if(entriesAtPrice.isEmpty()){
					productBookSide.clearIfEmpty(productBookSide.topOfBookPrice());
				}
				return fillMessages;
			}
		}
		
		for(Tradable t2 : tradedOut){
			entriesAtPrice.remove(t2);
		}
		if(entriesAtPrice.isEmpty()){
			productBookSide.clearIfEmpty(productBookSide.topOfBookPrice());
		}
		return fillMessages;
	}
}


