package publishers;

import java.util.ArrayList;
import java.util.HashMap;

import pricePackage.Price;
import pricePackage.PriceFactory;
import user.*;
import exceptions.*;

public class TickerPublisher {


	private static TickerPublisher ourInstance;
	private static HashMap <String, ArrayList<User>> userMap = new HashMap<>();
	private static HashMap <String, Price> priceMap = new HashMap<>();
	
	public static synchronized TickerPublisher getInstance(){
		
		if(ourInstance == null){
			ourInstance = new TickerPublisher();
		}
		return ourInstance;
	}
	
	private TickerPublisher(){
		
	}
	
	public synchronized void subscribe(User u, String product) throws AlreadySubscribedException{
		
		ArrayList<User> userList = userMap.get(product);
		
		//if List does not exist, create it and add subscription
		if(userList == null){
			userList = new ArrayList<User>();
			userList.add(u);
			userMap.put(product, userList);
		}
		
		//if List exists but already contains subscription
		else if(userList.contains(u)){
			throw new AlreadySubscribedException("User " + u + " is already subscribed");
		}
		
		//if List exists but does not contain subscription
		else{
			userList.add(u);
			userMap.put(product, userList);
		}
	}
	
	public synchronized void unSubscribe(User u, String product) throws NotSubscribedException {
		
		ArrayList<User> userList = userMap.get(product);
		
		//if product exists and contains subscription
		if(userList.contains(u)){
			userList.remove(u);
			userMap.put(product, userList);
		}
		
		//if product exists but does not contain subscription
		else{
			throw new NotSubscribedException("User " + u + " is not subscribed");
		}
	}
	
	public synchronized void publishTicker(String product, Price p){
		
		Price lastPrice = priceMap.get(product);
		char direction;
		Price newPrice; 
		
		if(p == null){
			newPrice = PriceFactory.makeLimitPrice(0);
		}
		else{
			newPrice = p;
		}	
		
		//document all cases
		if(lastPrice == null){;
			direction = ' ';
			priceMap.put(product, newPrice);
		}
		else if(lastPrice.equals(newPrice)){
			direction = '=';
			priceMap.put(product, newPrice);
		}
		else if(lastPrice.greaterThan(newPrice)){
			direction = 8595;
			priceMap.put(product, newPrice);
		}
		else{
			direction = 8593;
			priceMap.put(product, newPrice);		
		}
		
		//get product List
		ArrayList<User> userList = userMap.get(product);

		if(userList != null){
			for(int i = 0; i < userList.size(); i++){
				User u = userList.get(i);
				u.acceptTicker(product, newPrice, direction);
			}
		}
	}
}
