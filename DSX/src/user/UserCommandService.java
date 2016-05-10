package user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import exceptions.AlreadyConnectedException;
import exceptions.AlreadySubscribedException;
import exceptions.InvalidConnectionIdException;
import exceptions.InvalidMarketStateException;
import exceptions.InvalidVolumeOperation;
import exceptions.NoSuchProductException;
import exceptions.NotSubscribedException;
import exceptions.OrderNotFoundException;
import exceptions.UserNotConnectedException;
import book.ProductService;
import pricePackage.Price;
import publishers.CurrentMarketPublisher;
import publishers.LastSalePublisher;
import publishers.MessagePublisher;
import publishers.TickerPublisher;
import tradablePackage.Order;
import tradablePackage.Quote;
import tradablePackage.TradableDTO;

public class UserCommandService {
	
	private static UserCommandService ourInstance;
	private HashMap<String, Long> connectedIDs = new HashMap<>();
	private HashMap<String, User> connectedUsers = new HashMap<>();
	private HashMap<String, Long> connectedTimes = new HashMap<>();
	
	public static synchronized UserCommandService getInstance(){
		
		if(ourInstance == null){
			ourInstance = new UserCommandService();
		}
		return ourInstance;
	}
	
	private UserCommandService(){}

	/*
	 * Utility method that will be used by many methods in this class to verify
	 * the integrity of the user name and connection id passed in with many of
	 * the method calls found here
	 */
	private void verifyUser(String userName, long connId) throws UserNotConnectedException,
																 InvalidConnectionIdException{
		if(!connectedIDs.containsKey(userName)){
			throw new UserNotConnectedException("User " + userName + " not connected");
		}
		if(connectedIDs.get(userName) != connId){
			throw new InvalidConnectionIdException("Invalid Connection Id Exception");
		}
	}
	
	/*
	 * This method will connect the user to the trading system
	 */
	public synchronized long connect(User user) throws AlreadyConnectedException{
		if(connectedIDs.containsKey(user)){
			throw new AlreadyConnectedException("User " + user.getUserName() + " already connected");
		}
		connectedIDs.put(user.getUserName(), System.nanoTime());
		connectedUsers.put(user.getUserName(), user);
		connectedTimes.put(user.getUserName(), System.currentTimeMillis());
		
		return connectedIDs.get(user.getUserName()); // << POSSIBLE ERROR. User or GetUserName?
	}
	
	/*
	 * This method will disconnect the user from the trading system
	 */
	public synchronized void disConnect(String userName, long connId) throws UserNotConnectedException,
																			 InvalidConnectionIdException{
		verifyUser(userName, connId);
		connectedIDs.remove(userName);
		connectedUsers.remove(userName);
		connectedTimes.remove(userName);
	}
	
	/*
	 * Forwards the call of "getBookDepth" to the ProductService
	 */
	public String[][] getBookDepth(String userName, long connId, String product) throws NoSuchProductException,
																						UserNotConnectedException,
																						InvalidConnectionIdException{
		verifyUser(userName, connId);
		return ProductService.getInstance().getBookDepth(product);
	}
	
	/*
	 * Forwards the call of the "getMarketState" to the ProductService
	 */
	public String getMarketState(String userName, long connId) throws UserNotConnectedException,
																	  InvalidConnectionIdException{
		verifyUser(userName, connId);
		return ProductService.getInstance().getMarketState().toString();
	}
	
	/*
	 * Forwads the call of the "getOrdersWithRemainingQty" to the ProductService
	 */
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName, long connId, String product) throws UserNotConnectedException,
																															  InvalidConnectionIdException{
		verifyUser(userName, connId);
		return ProductService.getInstance().getOrdersWithRemainingQty(userName, product);
	}
	
	/*
	 * This method will return the sorted list of the available stocks on this system
	 * recieved from the ProductService
	 */
	public ArrayList<String> getProducts(String userName, long connId) throws UserNotConnectedException,
																			  InvalidConnectionIdException{
		verifyUser(userName, connId);
		ArrayList<String> temp = ProductService.getInstance().getProductList();
		Collections.sort(temp);
		return temp;
	}
	
	/*
	 * This method will create an order object using the data passed in, and will
	 * forward the order of the ProductService's submitOrder method
	 */
	public String submitOrder(String userName, long connId, String product, 
							  Price price, int volume, String side) throws InvalidVolumeOperation,
							  											   InvalidMarketStateException,
							  											   NoSuchProductException,
							  											   UserNotConnectedException,
							  											   InvalidConnectionIdException{
		verifyUser(userName, connId);
		Order order = new Order(userName, product, price, volume, side);
		return ProductService.getInstance().submitOrder(order);
	}
	
	/*
	 * This method will forward the provided information to ProductService's
	 * submitOrderCancel method
	 */
	public void submitOrderCancel(String userName, long connId, String product,
								  String side, String orderId) throws InvalidMarketStateException,
								  									  NoSuchProductException,
								  									  InvalidVolumeOperation,
								  									  UserNotConnectedException,
								  									  InvalidConnectionIdException,
								  									  OrderNotFoundException {
		verifyUser(userName, connId);
		ProductService.getInstance().submitOrderCancel(product, side, orderId);
	}
	
	/*
	 * This method will create a quote object using the data passed in, and will 
	 * forward the quote to the ProductService's submitQuote method
	 */
	public void submitQuote(String userName, long connId, String product, 
							Price bPrice, int bVolume, Price sPrice, int sVolume) throws InvalidVolumeOperation,
																						 InvalidMarketStateException,
																						 NoSuchProductException,
																						 UserNotConnectedException,
																						 InvalidConnectionIdException{
		verifyUser(userName, connId);
		Quote quote = new Quote(userName, product, bPrice, bVolume, sPrice, sVolume);
		ProductService.getInstance().submitQuote(quote);
	}
	
	/*
	 * This method will forward the provided data to the ProductService's 
	 * submitQuoteCancel method
	 */
	public void submitQuoteCancel(String userName, long connId, String product) throws InvalidMarketStateException,
																					   NoSuchProductException,
																					   UserNotConnectedException,
																					   InvalidConnectionIdException{
		verifyUser(userName, connId);
		ProductService.getInstance().submitQuoteCancel(userName, product);
	}
	
	/*
	 * This method will forward the subscription request to the CurrentMarketPublisher
	 */
	public void subscribeCurrentMarket(String userName, long connId, String product) throws AlreadySubscribedException,
																							UserNotConnectedException,
																							InvalidConnectionIdException{
		verifyUser(userName, connId);
		CurrentMarketPublisher.getInstance().subscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 * This method will forward the subscription request to the LastSalePublisher
	 */
	public void subscribeLastSale(String userName, long connId, String product) throws AlreadySubscribedException,
																					   UserNotConnectedException,
																					   InvalidConnectionIdException{
		verifyUser(userName, connId);
		LastSalePublisher.getInstance().subscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 * This method will forward the subscription request to the MessagePublisher
	 */
	public void subscribeMessages(String userName, long conn, String product) throws AlreadySubscribedException,
																					 UserNotConnectedException,
																					 InvalidConnectionIdException{
		verifyUser(userName, conn);
		MessagePublisher.getInstance().subscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 * This method will forward the subscription request to the TickerPublisher
	 */
	public void subscribeTicker(String userName, long conn, String product) throws AlreadySubscribedException,
																				   UserNotConnectedException,
																				   InvalidConnectionIdException{
		verifyUser(userName, conn);
		TickerPublisher.getInstance().subscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 * This method will forward the unsubscribe request to the CurrentMarketPublisher
	 */
	public void unSubscribeCurrentMarket(String userName, long conn, String product) throws NotSubscribedException,
																							UserNotConnectedException,
																							InvalidConnectionIdException{
		verifyUser(userName, conn);
		CurrentMarketPublisher.getInstance().unSubscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 *  This method will forward the unsubscribe request to the LastSalePublisher
	 */
	public void unSubscribeLastSale(String userName, long conn, String product) throws NotSubscribedException,
																					   UserNotConnectedException,
																					   InvalidConnectionIdException{
		verifyUser(userName, conn);
		LastSalePublisher.getInstance().unSubscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 *  This method will forward the unsubscribe request to the TickerPublisher
	 */
	public void unSubscribeTicker(String userName, long conn, String product) throws NotSubscribedException,
																					 UserNotConnectedException,
																					 InvalidConnectionIdException{
		verifyUser(userName, conn);
		TickerPublisher.getInstance().unSubscribe(connectedUsers.get(userName), product);
	}
	
	/*
	 *  This method will forward the unsubscribe request to the MessagePublisher
	 */
	public void unSubscribeMessages(String userName, long conn, String product) throws NotSubscribedException,
																					   UserNotConnectedException,
																					   InvalidConnectionIdException{
		verifyUser(userName, conn);
		MessagePublisher.getInstance().unSubscribe(connectedUsers.get(userName), product);
	}
}
