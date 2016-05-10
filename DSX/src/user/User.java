package user;

import java.util.ArrayList;

import exceptions.AlreadyConnectedException;
import exceptions.AlreadySubscribedException;
import exceptions.InvalidConnectionIdException;
import exceptions.InvalidMarketStateException;
import exceptions.InvalidPriceOperation;
import exceptions.InvalidVolumeOperation;
import exceptions.NoSuchProductException;
import exceptions.OrderNotFoundException;
import exceptions.UserNotConnectedException;
import pricePackage.*;
import tradablePackage.TradableDTO;
import messages.*;

public interface User {

	String getUserName();
	void acceptLastSale(String product, Price p, int v);
	void acceptMessage(FillMessage fm);
	void acceptMessage(CancelMessage cm);
	void acceptMarketMessage(String message);
	void acceptTicker(String product, Price p, char direction);
	void acceptCurrentMarket(String product, Price bp, int bv, Price sp, int sv );
	
	void connect() throws AlreadyConnectedException, UserNotConnectedException, InvalidConnectionIdException; // Instructs a User object to connect to the trading system.
	void disConnect() throws UserNotConnectedException, InvalidConnectionIdException; // Instructs a User object to disconnect from the trading system.
	void showMarketDisplay() throws UserNotConnectedException, Exception; // Requests the opening of the market display if the user is connected.
	String submitOrder(String product, Price price, int volume, String side) throws InvalidVolumeOperation, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to submit a new Order request
	void submitOrderCancel(String product, String side, String orderId) throws InvalidMarketStateException, NoSuchProductException, InvalidVolumeOperation, UserNotConnectedException, InvalidConnectionIdException, OrderNotFoundException; // Allows the User object to submit a new Order Cancel request
	void submitQuote(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) throws InvalidVolumeOperation, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to submit a new Quote request
	void submitQuoteCancel(String product) throws InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to submit a new Quote Cancel request
	void subscribeCurrentMarket(String product) throws AlreadySubscribedException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to subscribe for Current Market for the specified Stock.
	void subscribeLastSale(String product) throws AlreadySubscribedException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to subscribe for Last Sale for the specified Stock.
	void subscribeMessages(String product) throws AlreadySubscribedException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to subscribe for Messages for the specified Stock.
	void subscribeTicker(String product) throws AlreadySubscribedException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to subscribe for Ticker for the specified Stock.
	Price getAllStockValue() throws InvalidPriceOperation; // Returns the value of the all Sock the User owns (has bought but not sold)
	Price getAccountCosts(); // Returns the difference between cost of all stock purchases and stock sales
	Price getNetAccountValue() throws InvalidPriceOperation; // Returns the difference between current value of all stocks owned and the account costs
	String[][] getBookDepth(String product) throws NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to submit a Book Depth request for the specified stock.
	String getMarketState() throws UserNotConnectedException, InvalidConnectionIdException; // Allows the User object to query the market state (OPEN, PREOPEN, CLOSED).
	ArrayList<TradableUserData> getOrderIds(); // Returns a list of order id’s for the orders this user has submitted.
	ArrayList<String> getProductList(); // Returns a list of the stock products available in the trading system.
	Price getStockPositionValue(String sym) throws InvalidPriceOperation; // Returns the value of the specified stock that this user owns
	int getStockPositionVolume(String product); // Returns the volume of the specified stock that this user owns
	ArrayList<String> getHoldings(); // Returns a list of all the Stocks the user owns
	ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) throws UserNotConnectedException, InvalidConnectionIdException; // Gets a list of DTO’s containing information on all Orders for this user for the specified product with remaining volume.
}
