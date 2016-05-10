package pricePackage;

import java.util.HashMap;

public class PriceFactory {
	
	private static HashMap <Long, Price> priceMap = new HashMap<>();
	
	public static Price makeLimitPrice(String value){		
		
		//Converts string to long value
		String s = value.replaceAll("[$,]","");
		double d = Double.parseDouble(s);
		d = d*100;
		long l = (long) d;
		
		//Creates new price object and gets it from the hashMap
		Price price = priceMap.get(l);
		
		//Checks to see if the price object is already in the hashMap
		//If it is not in there, it will put the new price object in the hashMap 
		if(price == null){
			price = new Price(l);
			priceMap.put(l, price);
		}
		
		//returns the price object to the constructor
		return price;
	}
	
	public static Price makeLimitPrice(long value){
		
		//Creates new price object and gets it from the hashMap
		Price price = priceMap.get(value);
		
		//Checks to see if the price object is already in the hashMap
		//If it is not in there, it will put the new price object in the hashMap 
		if(price == null){
			price = new Price(value);
			priceMap.put(value, price);
		}
		
		//returns the price object to the constructor
		return price;
	}
	
	public static Price makeMarketPrice(){
		
		Boolean MKT = true;
		long k = MKT.hashCode();
		
		Price price = priceMap.get(k);
		
		if(priceMap.containsKey(k) == true){
			price = priceMap.get(k);
			return price;
		}
		
		price = new Price();
		priceMap.put(k, price);
		return price;
		/*
		//Creates new price object and gets it from the hashMap
		Price price = (Price)priceMap.get(0);
		
		
		//Checks to see if the price object is already in the hashMap
		//If it is not in there, it will put the new price object in the hashMap 
		if(price == null){
			price = new Price();
			priceMap.put((long) 0, price);
		}
		
		//returns price object with value of 0 (MKT) to constructor
		return price;
		*/
	}
}
