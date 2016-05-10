package publishers;

import pricePackage.*;

public class MarketDataDTO {

	public String product;
	public Price buyPrice;
	public int buyVolume;
	public Price sellPrice;
	public int sellVolume;
	
	public MarketDataDTO(String p, Price bp, 
						 int bv, Price sp, int sv){
		product = p;
		buyPrice = bp;
		buyVolume = bv;
		sellPrice = sp;
		sellVolume = sv;
	}
	
	public String getProduct(){ //throw exception
		return product;
	}
	public Price getBuyPrice(){
		return buyPrice;
	}
	public int getBuyVolume(){
		return buyVolume;
	}
	public Price getSellPrice(){
		return sellPrice;
	}
	public int getSellVolume(){
		return sellVolume;
	}
	
	public String toString(){
		return "Product: " + product + ", Buy Price: " + buyPrice
				+ ", Buy Volume: " + buyVolume + ", Sell Price: "
				+  sellPrice + ", Sell Volume: " + sellVolume;
	}
	
}
