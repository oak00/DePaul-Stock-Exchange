package publishers;

import java.util.ArrayList;
import java.util.HashMap;

import user.*;
import pricePackage.*;
import exceptions.*;

public final class CurrentMarketPublisher {

	private static CurrentMarketPublisher ourInstance;
	private static HashMap <String, ArrayList<User>> productMap = new HashMap();
	
	public static synchronized CurrentMarketPublisher getInstance(){
		
		if(ourInstance == null){
			ourInstance = new CurrentMarketPublisher();
		}
		return ourInstance;
	}
	
	private CurrentMarketPublisher(){
		
	}
	
	public synchronized void subscribe(User u, String product) throws AlreadySubscribedException{
		
		ArrayList<User> userList = productMap.get(product);
		
		//if List does not exist, create it and add subscription
		if(userList == null){
			userList = new ArrayList<User>();
			userList.add(u);
			productMap.put(product, userList);
		}
		
		//if List exists but already contains subscription
		else if(userList.contains(u)){
			throw new AlreadySubscribedException("User " + u + " is already subscribed");
		}
		
		//if List exists but does not contain subscription
		else{
			userList.add(u);
			productMap.put(product, userList);
		}
	}
	
	public synchronized void unSubscribe(User u, String product) throws NotSubscribedException {
		
		ArrayList<User> userList = productMap.get(product);
		
		//if product exists and contains subscription
		if(userList.contains(u)){
			userList.remove(u);
			productMap.put(product, userList);
		}
		
		//if product exists but does not contain subscription
		else{
			throw new NotSubscribedException("User " + u + " is not subscribed");
		}
	}
	
	public synchronized void publishCurrentMarket(MarketDataDTO md){
		
		Price buyPrice;
		Price sellPrice;
		
		//gets product name from DTO
		String dtoProduct = md.getProduct();
		ArrayList<User> userList = productMap.get(dtoProduct);
		
		if(md.getBuyPrice() == null){
			buyPrice = PriceFactory.makeLimitPrice(0);
		}
		else{
			buyPrice = md.getBuyPrice();
		}
		
		if(md.getSellPrice() == null){
			sellPrice = PriceFactory.makeLimitPrice(0);
		}
		else{
			sellPrice = md.getSellPrice();
		}		
		
		if(userList != null){
		for(int i = 0; i < userList.size(); i++){
			User u = userList.get(i);
			u.acceptCurrentMarket(md.getProduct(), buyPrice, md.getBuyVolume(),
								  sellPrice, md.getSellVolume());
		}
		}
		
	}
	
}
