package publishers;

import java.util.*;

import messages.*;
import pricePackage.Price;
import user.*;
import exceptions.*;

public class MessagePublisher {

	private static MessagePublisher ourInstance;
	private static HashMap <String, ArrayList<User>> userMap = new HashMap<>();
	
	public static synchronized MessagePublisher getInstance(){
		
		if(ourInstance == null){
			ourInstance = new MessagePublisher();
		}
		return ourInstance;
	}
	
	private MessagePublisher(){
		
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
	
	public synchronized void publishCancel(CancelMessage cm){
		
		ArrayList<User> userList = userMap.get(cm.getProduct());
		if(userList!=null){
		int i = userList.indexOf(cm.getUser());
		User u = userList.get(i+1);
		u.acceptMessage(cm);
		}
	}
	
	public synchronized void publishFill(FillMessage fm){
		
		ArrayList<User> userList = userMap.get(fm.getProduct());
		if(userList != null){
		int i = userList.indexOf(fm.getUser());
		User u = userList.get(i+1);
		u.acceptMessage(fm);
		}
	}
	
	public synchronized void publishMarketMessage(MarketMessage mm){
		
		ArrayList<User> userList = new ArrayList<>();
		
		for(String product : userMap.keySet()){
			for(User user : userMap.get(product)){
				if(!userList.contains(user)){
					userList.add(user);
				}
			}
		}
		
		for(User user : userList){
			user.acceptMarketMessage(mm.getState());
		}
	}
}
