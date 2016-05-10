package tradablePackage;

import exceptions.InvalidPriceOperation;
import exceptions.InvalidVolumeOperation;
import pricePackage.Price;
import pricePackage.PriceFactory;

public class Quote {

	private String user;
	private String product;
	private QuoteSide BUY;
	private QuoteSide SELL;
	
	public Quote(String userName, String productSymbol, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) throws InvalidVolumeOperation {
		user = userName;
		product = productSymbol;
		if(sellVolume < 0){
			throw new InvalidVolumeOperation("Invalid SELL-Side Volume: " + sellVolume);
		}
		else{
			BUY = new QuoteSide(user, product, buyPrice, buyVolume,"BUY");
			SELL = new QuoteSide(user, product, sellPrice, sellVolume, "SELL");
		}
	}
	
	public String getUserName(){
		return user;
	}
	
	public String getProduct(){
		return product;
	}
	
	public QuoteSide getQuoteSide(String sideIn){
		if( sideIn.equals("BUY")){
			return BUY;
		}
		else{
			return SELL;
		}
	}
	
	public String toString(){
		return user + " " + product  + " " + BUY + " - " + SELL; 
	}
}
