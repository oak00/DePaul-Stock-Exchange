package user;

import exceptions.AlreadyConnectedException;
import exceptions.AlreadySubscribedException;
import exceptions.InvalidConnectionIdException;
import exceptions.InvalidMarketStateException;
import exceptions.InvalidPriceOperation;
import exceptions.InvalidVolumeOperation;
import exceptions.NoSuchProductException;
import exceptions.OrderNotFoundException;
import exceptions.UserNotConnectedException;
import gui.UserDisplayManager;

import java.sql.Timestamp;
import java.util.ArrayList;

import messages.CancelMessage;
import messages.FillMessage;
import pricePackage.Price;
import tradablePackage.TradableDTO;

public class UserImpl implements User{

	private String userName;
	private long connectionId;
	private ArrayList<String> stocks = new ArrayList<>();
	private ArrayList<TradableUserData> tudList = new ArrayList<>();
	private Position position;
	private UserDisplayManager udm; // << POSSIBLE NULL INITIALIZATION ERROR
	
	//Constructor
	public UserImpl(String userName){
		setUserName(userName);
		position = new Position();
	}
	
	/*
	 * Sets user name
	 */
	private void setUserName(String u){ // Throw Exception
		if(u == null){
			throw new NullPointerException("UserName input parameter cannot be null");
		}
		userName = u;
	}
	
	/*
	 * returns the username of  this user
	 */
	public String getUserName(){
		return userName;
	}
	
	/*
	 * This method should call the user display manager's updateLastSale method
	 */
	public void acceptLastSale(String product, Price p, int v) {
		try{
			udm.updateLastSale(product, p, v);
			position.updateLastSale(product, p);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * This method will display the fill message in the market display and will
	 * forward the data to the Position object
	 */
	public void acceptMessage(FillMessage fm) { // << WRONG FORMAT FOR STRING
		try{
			Timestamp t = new Timestamp(System.currentTimeMillis());
			String s = new String(fm.toString());
			udm.updateMarketActivity(t + s);
			position.updatePosition(fm.getProduct(), fm.getPrice(), fm.getSide(), fm.getVolume());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * This method will display the Cancel Message in the market display
	 */
	public void acceptMessage(CancelMessage cm) { // << WRONG FORMAT FOR STRING
		try{
			Timestamp t = new Timestamp(System.currentTimeMillis());
			String s = new String(cm.toString());
			udm.updateMarketActivity(t + s);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * This method will display the Market Message in the market display
	 */
	public void acceptMarketMessage(String message) {
		try{
			udm.updateMarketState(message);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * This method will display the Ticker data in the market display
	 */
	public void acceptTicker(String product, Price p, char direction) {
		try{
			udm.updateTicker(product, p, direction);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * This method will display the Current Market data in the market display
	 */
	public void acceptCurrentMarket(String product, Price bp, int bv, Price sp, int sv) {
		try{
			udm.updateMarketData(product, bp, bv, sp, sv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * This method will connect the user to the trading system
	 */
	public void connect() throws AlreadyConnectedException,
								 UserNotConnectedException,
								 InvalidConnectionIdException {
		connectionId = UserCommandService.getInstance().connect(this);
		stocks = UserCommandService.getInstance().getProducts(userName, connectionId);
	}

	/*
	 * This method will disconnect the user from the trading system
	 */
	public void disConnect() throws UserNotConnectedException,
									InvalidConnectionIdException {
		UserCommandService.getInstance().disConnect(userName, connectionId);
	}

	/*
	 * This method will activate the market display
	 */
	public void showMarketDisplay() throws Exception, UserNotConnectedException {
		if(stocks == null){
			throw new UserNotConnectedException("User " + userName + " is not connected");
		}
		if(udm == null){
			udm = new UserDisplayManager(this);
		}
		udm.showMarketDisplay();
	}

	/*
	 * This method forwards the new order request to the user command service 
	 * and saves the resulting order id
	 */
	public String submitOrder(String product, Price price, int volume, String side) throws InvalidVolumeOperation,
																						   InvalidMarketStateException,
																						   NoSuchProductException,
																						   UserNotConnectedException,
																						   InvalidConnectionIdException {
		
		String orderId = UserCommandService.getInstance().submitOrder(userName, connectionId, product, price, volume, side);
		TradableUserData tud = new TradableUserData(userName, product, side, orderId);
		tudList.add(tud);
		return orderId;
	}

	/*
	 * This method forwards the order cancel request to the user command service
	 */
	public void submitOrderCancel(String product, String side, String orderId) throws InvalidMarketStateException,
																					  NoSuchProductException,
																					  InvalidVolumeOperation,
																					  UserNotConnectedException,
																					  InvalidConnectionIdException,
																					  OrderNotFoundException {
		UserCommandService.getInstance().submitOrderCancel(userName, connectionId, product, side, orderId);
		
	}

	/*
	 * This method forwards the new quote request to the user command service
	 */
	public void submitQuote(String product, Price buyPrice, int buyVolume, 
							Price sellPrice, int sellVolume) throws InvalidVolumeOperation,
																	InvalidMarketStateException,
																	NoSuchProductException,
																	UserNotConnectedException,
																	InvalidConnectionIdException {
		UserCommandService.getInstance().submitQuote(userName, connectionId, product, buyPrice, buyVolume, sellPrice, sellVolume);
	}

	/*
	 * This method forwards the quote cancel request to the user command service
	 */
	public void submitQuoteCancel(String product) throws InvalidMarketStateException,
														 NoSuchProductException,
														 UserNotConnectedException,
														 InvalidConnectionIdException {
		UserCommandService.getInstance().submitQuoteCancel(userName, connectionId, product);
	}

	/*
	 * This method forwards the current market subscription to the user command service
	 */
	public void subscribeCurrentMarket(String product) throws AlreadySubscribedException,
															  UserNotConnectedException,
															  InvalidConnectionIdException {
		UserCommandService.getInstance().subscribeCurrentMarket(userName, connectionId, product);
	}

	/*
	 * This method forwards the last sale subscription to the user command service
	 */
	public void subscribeLastSale(String product) throws AlreadySubscribedException,
														 UserNotConnectedException,
														 InvalidConnectionIdException {
		UserCommandService.getInstance().subscribeLastSale(userName, connectionId, product);
	}

	/*
	 * This method forwards the message subscription to the user command service
	 */
	public void subscribeMessages(String product) throws AlreadySubscribedException,
														 UserNotConnectedException,
														 InvalidConnectionIdException {
		UserCommandService.getInstance().subscribeMessages(userName, connectionId, product);
	}

	/*
	 * This method forwards the ticker subscription to the user command service
	 */
	public void subscribeTicker(String product) throws AlreadySubscribedException,
													   UserNotConnectedException,
													   InvalidConnectionIdException {
		UserCommandService.getInstance().subscribeTicker(userName, connectionId, product);
	}

	/*
	 * Returns the value of all the stock the user owns
	 */
	public Price getAllStockValue() throws InvalidPriceOperation {
		return this.position.getAllStockValue();
	}

	/*
	 * Returns the difference between the cost of all stock purchases and stock sales
	 */
	public Price getAccountCosts() {
		return this.position.getAccountCosts();
	}

	/*
	 * Returns the difference between the current value of all stocks owned
	 * and the account costs 
	 */
	public Price getNetAccountValue() throws InvalidPriceOperation {
		return this.position.getNetAccountValue();
	}

	/*
	 * Allows the User object to submit a Book Depth request for the specified stock
	 */
	public String[][] getBookDepth(String product) throws NoSuchProductException,
														  UserNotConnectedException,
														  InvalidConnectionIdException {
		return UserCommandService.getInstance().getBookDepth(userName, connectionId, product);
	}

	/*
	 * Allows the User object to query the market state
	 */
	public String getMarketState() throws UserNotConnectedException,
										  InvalidConnectionIdException {
		return UserCommandService.getInstance().getMarketState(userName, connectionId);
	}

	/*
	 * Returns a list of order id's for the orders this user has submitted
	 */
	public ArrayList<TradableUserData> getOrderIds() {
		return tudList;
	}

	/*
	 * Returns a list of stocks available in the trading system
	 */
	public ArrayList<String> getProductList() {
		return stocks;
	}

	/*
	 * Returns the value of the specified stock that this user owns
	 */
	public Price getStockPositionValue(String product) throws InvalidPriceOperation {
		return this.position.getStockPositionValue(product);
	}

	/*
	 * Returns the volume of the specified stock that this user owns
	 */
	public int getStockPositionVolume(String product) {
		return this.position.getStockPositionVolume(product);
	}

	/*
	 * Returns a list of all the stocks the user owns
	 */
	public ArrayList<String> getHoldings() {
		return this.position.getHoldings();
	}

	/*
	 * Gets a list of DTOs containing information on all Orders for this user for 
	 * the specified product with remaining volume
	 */
	public ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) throws UserNotConnectedException,
																				   InvalidConnectionIdException {
		return UserCommandService.getInstance().getOrdersWithRemainingQty(userName, connectionId, product);
	}
	
	
}
