package tradablePackage;

import exceptions.InvalidVolumeOperation;
import pricePackage.Price;

public class TradableImpl implements Tradable {

	private String user;
	private String product;
	private String id;
	private String bookSide;
	private Price price;
	private int originalOrderVolume;
	private int remainingOrderVolume;
	private int cancelledOrderVolume;
	
	public TradableImpl(String userName, String productSymbol, Price orderPrice, int originalVolume, String side) throws InvalidVolumeOperation {
		user = userName;
		product = productSymbol;
		price = orderPrice;
		if(originalVolume < 1){
			throw new InvalidVolumeOperation("Invalid Order Volume: " + originalVolume);
		}
		else{
		originalOrderVolume = originalVolume;
		remainingOrderVolume = originalVolume;
		bookSide = side;
		id = user + product + price + System.nanoTime();
		}
	}
	
	public String getProduct(){
		return product;
	}
	
	public Price getPrice(){
		return price;
	}
	
	public int getOriginalVolume(){
		return originalOrderVolume;
	}
	
	public int getRemainingVolume(){
		return remainingOrderVolume;
	}
	
	public int getCancelledVolume(){
		return cancelledOrderVolume;
	}
	
	public void setCancelledVolume(int newCancelledVolume) throws InvalidVolumeOperation{
		if(newCancelledVolume < 0){
			throw new InvalidVolumeOperation("Cancelled Volume cannot be negative");
		}
		if(((newCancelledVolume + getRemainingVolume()) > getOriginalVolume())){
			throw new InvalidVolumeOperation("Cancelled Volume plus current Remaining Volume cannot exceed original Volume");
		}
		else{
			cancelledOrderVolume = newCancelledVolume;
		}

	}
	
	public void setRemainingVolume(int newRemainingVolume) throws InvalidVolumeOperation{
		if(newRemainingVolume < 0){
			throw new InvalidVolumeOperation("Cancelled Volume cannot be negative");
		}
		if(((newRemainingVolume + getCancelledVolume()) > getOriginalVolume())){
			throw new InvalidVolumeOperation("Cancelled Volume plus current Remaining Volume cannot exceed original Volume");
		}
		else{
		remainingOrderVolume = newRemainingVolume;
		}
	}
	
	public String getUser(){
		return user;
	}
	
	public String getSide(){
		return bookSide;
	}
	
	public boolean isQuote(){
		return true;
	}
	
	public String getId(){
		return id;
	}
	
}
