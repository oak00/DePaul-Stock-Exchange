package book;

import java.util.ArrayList;
import java.util.HashMap;

import exceptions.DataValidationException;
import exceptions.InvalidMarketStateException;
import exceptions.InvalidMarketStateTransition;
import exceptions.InvalidMessageState;
import exceptions.InvalidVolumeOperation;
import exceptions.NoSuchProductException;
import exceptions.OrderNotFoundException;
import exceptions.ProductAlreadyExistsException;
import messages.MarketMessage;
import pricePackage.PriceFactory;
import publishers.CurrentMarketPublisher;
import publishers.MarketDataDTO;
import publishers.MessagePublisher;
import tradablePackage.Order;
import tradablePackage.Quote;
import tradablePackage.TradableDTO;

/*The ProductService is the Facade to the entities that make up the
 * Products and the Product Books. All interaction with the product
 * book and the buy and sell sides of a stock's book will go through
 * this Facade.  
 */

public class ProductService {

	private String currentMarketState = "CLOSED";
	private HashMap<String, ProductBook> allBooks = new HashMap<>();
	private static ProductService ourInstance;
	
	//Singleton initialization
	public static synchronized ProductService getInstance(){
		
		if(ourInstance == null){
			ourInstance = new ProductService();
		}
		return ourInstance;
	}
	
	private ProductService(){
		
	}
	
	//Query Methods
	
	/*
	 * Returns a list of TradableDTOs containing any orders with remaining
	 * quantity for the user and stock specified
	 */
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName, String product){
		return allBooks.get(product).getOrdersWithRemainingQty(userName);
	}
	
	/*
	 *Returns a list of MarketDataDTOs containing the best buy price/volume
	 *and sell price/volume for the specified stock product 
	 */
	public synchronized MarketDataDTO getMarketData(String product){
		return allBooks.get(product).getMarketData();
	}
	
	/*
	 * Returns the current product state
	 */
	public synchronized String getMarketState(){
		return currentMarketState;
	}
	
	/*
	 * Checks that the product is a real stock symbol. If not, throws a
	 * NoSuchProductException. Otherwise, get the ProductBook from the
	 * allBooks HashMap using the String stock symbol passed in as the
	 * key.
	 */
	public synchronized String[][] getBookDepth(String product) throws NoSuchProductException {
		if(!allBooks.containsKey(product)){
			throw new NoSuchProductException(product + " does not exist");
		}
		return allBooks.get(product).getBookDepth();
	}
	
	/*
	 * Returns an ArrayList containing all the keys in allBooks
	 */
	public synchronized ArrayList<String> getProductList(){
		return new ArrayList<String>(allBooks.keySet());
	}
	
	//Market And Product Service Manipulation Methods
	
	/*
	 * Updates the market state to the new value passed in. Must
	 * go from CLOSED -> PREOPEN -> OPEN
	 */
	public synchronized void setMarketState(String ms) throws InvalidMarketStateTransition, 
															  InvalidMessageState,
															  InvalidVolumeOperation,
															  OrderNotFoundException {
		
		
		// If the market does not follow the required protocol, throw and exception
		if((currentMarketState.equals("CLOSED") && !ms.equals("PREOPEN")) &&
				(currentMarketState.equals("CLOSED") && !ms.equals("CLOSED"))){
			throw new InvalidMarketStateTransition("Cannot go from CLOSED to " + ms + ". CLOSED must either stay as CLOSED or proceed to PREOPEN");
		}
		else if(currentMarketState.equals("PREOPEN") && !ms.equals("OPEN")){
			throw new InvalidMarketStateTransition("Cannot go from PREOPEN to " + ms + ". PREOPEN must proceed to OPEN");
		}
		else if(currentMarketState.equals("OPEN") && !ms.equals("CLOSED")){
			throw new InvalidMarketStateTransition("Cannot go from OPEN to " + ms + ". OPEN must proceed to CLOSED");
		}
		
		//Sets current market state to the the value passed in, then publishes a market message
		currentMarketState = ms;
		MessagePublisher.getInstance().publishMarketMessage(new MarketMessage(currentMarketState));
		
		//If market state is open, opens all books
		if(currentMarketState.equals("OPEN")){
			for(String product : allBooks.keySet()){
				allBooks.get(product).openMarket();
			}
		}
		
		//If market state is closed, then closes all books
		if(currentMarketState.equals("CLOSED")){
			for(String product : allBooks.keySet()){
				allBooks.get(product).closeMarket();
			}
		}
	}
	
	/*
	 * Creates a new stock product that can be used for trading
	 */
	public synchronized void createProduct(String product) throws DataValidationException, ProductAlreadyExistsException {
		//Checks if passed in value contains anything
		if(product == null || product.isEmpty()){
			throw new DataValidationException("String entered is either null or empty");
		}
		//Checks if allBooks already has the product
		else if(allBooks.containsKey(product)){
			throw new ProductAlreadyExistsException(product + " already exists.");
		}
		//If not, creates new product
		else{
			allBooks.put(product, new ProductBook(product));
		}
	}
	
	/*
	 * Forwards the provided Quote to the appropriate product book
	 */
	public synchronized void submitQuote(Quote q) throws InvalidMarketStateException, NoSuchProductException, InvalidVolumeOperation{
		//Checks to see if 
		if(currentMarketState.equals("CLOSED")){
			throw new InvalidMarketStateException("Market is closed.");
		}
		else if(!allBooks.containsKey(q.getProduct())){
			throw new NoSuchProductException(q.getProduct() + " does not exist.");
		}
		else{
			allBooks.get(q.getProduct()).addToBook(q);
		}
	}
	
	/*
	 * Forwards the provided Order to the appropriate product book
	 */
	public synchronized String submitOrder(Order o) throws InvalidMarketStateException, NoSuchProductException, InvalidVolumeOperation {
		if(currentMarketState.equals("CLOSED")){
			throw new InvalidMarketStateException("Market is closed.");
		}
		//MKT orders cannot be submitted during PREOPEN
		else if(currentMarketState.equals("PREOPEN") && o.getPrice().equals("MKT")){
			throw new InvalidMarketStateException("MKT orders cannot be submitted during PREOPEN");
		}
		else if(!allBooks.containsKey(o.getProduct())){
			throw new NoSuchProductException(o.getProduct() + " does not exist.");
		}
		else{
			allBooks.get(o.getProduct()).addToBook(o);
			return o.getId();
		}
	}
	
	/*
	 * Forwards the provided Order Cancel to the appropriate product book
	 */
	public synchronized void submitOrderCancel(String product, String side, String orderId) throws InvalidMarketStateException, 
																								   NoSuchProductException,
																								   InvalidVolumeOperation,
																								   OrderNotFoundException {
		if(currentMarketState.equals("CLOSED")){
			throw new InvalidMarketStateException("Market is closed.");
		}
		else if(!allBooks.containsKey(product)){
			throw new NoSuchProductException(product + " does not exist");
		}
		else{
			allBooks.get(product).cancelOrder(side, orderId);
		}
	}
	
	/*
	 * Forwards the provided Quote Cancel to the appropriate product book
	 */
	public synchronized void submitQuoteCancel(String userName, String product) throws InvalidMarketStateException, NoSuchProductException{
		if(currentMarketState.equals("CLOSED")){
			throw new InvalidMarketStateException("Market is closed.");
		}
		else if(!allBooks.containsKey(product)){
			throw new NoSuchProductException(product + " does not exist");
		}
		else{
			allBooks.get(product).cancelQuote(userName);
		}
	}
	
	public static void main(String[] args) throws InvalidMarketStateTransition, InvalidMessageState, InvalidVolumeOperation{
        System.out.println("TS3.1) Change Market State to Closed then PreOpen the Market State. Rex & ANN should receive one market message for each state");
        try {
            ProductService.getInstance().setMarketState("CLOSED");
            ProductService.getInstance().setMarketState("PREOPEN");
        } catch (Exception ex) {
            System.out.println("Set market State caused an unexpected exception: " + ex.getMessage());
        }
        System.out.println();
	}
	
}
