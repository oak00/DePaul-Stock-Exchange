package tradablePackage;

import exceptions.InvalidPriceOperation;
import pricePackage.Price;
import pricePackage.PriceFactory;

public class TradableDTO {

	public String product;
	public Price price;
	public int originalVolume;
	public int remainingVolume;
	public int cancelledVolume;
	public String user;
	public String bookSide;
	public boolean isQuote;
	public String id;
	
	public TradableDTO(String productName, Price priceAmount, int originalOrderVolume, int remainingOrderVolume,
			           int cancelledOrderVolume, String userName, String side, boolean quote, String idNumber){
		
		product = productName;
		price = priceAmount;
		originalVolume = originalOrderVolume;
		remainingVolume = remainingOrderVolume;
		cancelledVolume = cancelledOrderVolume;
		user = userName;
		bookSide = side;
		isQuote = quote;
		id = idNumber;
	}
	
	public String toString(){
		return "Product: " + product + ", Price: " + price + ", OriginalVolume: " + originalVolume + ", RemainingVolume: " +
			   remainingVolume + ", CancelledVolume: " + cancelledVolume + ", User: " + user + ", Side: " + bookSide  +
			   ", IsQuote: " + isQuote + ", Id: " + id;  
	}
	
}
