package user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import exceptions.InvalidPriceOperation;
import pricePackage.Price;
import pricePackage.PriceFactory;

public class Position {

	private HashMap<String, Integer> holdings = new HashMap<>();
	private HashMap<String, Price> lastSales = new HashMap<>();
	private Price accountCost = PriceFactory.makeLimitPrice(0);

	//Constructor
	public Position(){}
	
	/*
	 * This method will update the holdings list and the account costs when
	 * some market activity occurs
	 */
	public void updatePosition(String product, Price price, String side, int volume) throws InvalidPriceOperation{
		
		int adjustedVolume = 0;
		
		if(side.equals("BUY")){
			adjustedVolume = volume;
		}
		if(side.equals("SELL")){
			adjustedVolume *= -1;
		}
		
		if(!holdings.containsKey(product)){
			holdings.put(product, adjustedVolume);
		}
		else{
			if((adjustedVolume += holdings.get(product)) == 0){
				holdings.remove(product);
			}
			else{
				adjustedVolume += holdings.get(product);
				holdings.put(product, adjustedVolume);
			}
		}
		
		Price totalPrice = price.multiply(volume);
		
		if(side.equals("BUY")){
			accountCost = accountCost.subtract(totalPrice);
		}
		else{
			accountCost = accountCost.add(totalPrice);
		}
	}
	
	/*
	 * This method will insert the last sale for the specified stock into
	 * the lastSales HashMap
	 */
	public void updateLastSale(String product, Price price){
		lastSales.put(product, price);
	}
	
	/*
	 * This method will return the volume of the specified stock this user
	 * owns. If the holdings HashMap does not have the product passsed in as 
	 * a key, then the user does not own this stock.
	 */
	public int getStockPositionVolume(String product){
		if(!holdings.containsKey(product)){
			return 0;
		}
		
		return holdings.get(product);
	}
	
	/*
	 * This method will return a sorted ArrayList of Strings containing the
	 * stock symbols this user owns
	 */
	public ArrayList<String> getHoldings(){			// << POSSIBLE NULL ERROR
		ArrayList<String> h = new ArrayList<>(holdings.keySet());
		Collections.sort(h);
		return h;
	}
	
	/*
	 * This method will return the current value of the stock symbol
	 * passed in that is owned by the user
	 */
	public Price getStockPositionValue(String product) throws InvalidPriceOperation{
		if(!holdings.containsKey(product)){
			return PriceFactory.makeLimitPrice(0);
		}
		
		if(lastSales.get(product) == null){
			return PriceFactory.makeLimitPrice(0);
		}
		
		return lastSales.get(product).multiply(holdings.get(product));
	}
	
	/*
	 * Returns the accountCost data member
	 */
	public Price getAccountCosts(){
		return accountCost;
	}
	
	/*
	 * This method returns the total current value of all stocks this user owns
	 */
	public Price getAllStockValue() throws InvalidPriceOperation{ // POSSIBLE LOOP BUG
		Price sumValue = PriceFactory.makeLimitPrice(0);
		
		for(String stock : holdings.keySet()){
			sumValue.add(getStockPositionValue(stock)); // << RIGHT HERE
		}
		return sumValue;
	}
	
	/*
	 * Returns the total current value of all stocks plus the account costs
	 */
	public Price getNetAccountValue() throws InvalidPriceOperation{
		return getAccountCosts().add(getAllStockValue());
	}
	
}
