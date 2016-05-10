package publishers;

import java.util.ArrayList;
import java.util.HashMap;

import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import pricePackage.Price;
import pricePackage.PriceFactory;
import user.*;
import exceptions.*;

public final class LastSalePublisher {

	private static LastSalePublisher ourInstance;
	private static HashMap <String, ArrayList<User>> productMap = new HashMap();
	
	public static synchronized LastSalePublisher getInstance(){
		
		if(ourInstance == null){
			ourInstance = new LastSalePublisher();
		}
		return ourInstance;
	}
	
	private LastSalePublisher(){
		
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
	
	public synchronized void publishLastSale(String product, Price p, int v){
		
		Price lastSale;
		
		if(p == null){
			lastSale = PriceFactory.makeLimitPrice(0);
		}
		else{
			lastSale = p;
		}	
		
		
		//get product List
		ArrayList<User> userList = productMap.get(product);

		if(userList != null){
			for(int i = 0; i < userList.size(); i++){
				User u = userList.get(i);
				u.acceptLastSale(product, lastSale, v);
			}
			TickerPublisher.getInstance().publishTicker(product,lastSale);
		}
		
	}
	
}
