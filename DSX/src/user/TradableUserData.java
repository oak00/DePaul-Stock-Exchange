package user;

public class TradableUserData {

	private String userName;
	private String product;
	private String side;
	private String orderId;
	
	public TradableUserData(String u, String p, String s, String id){
		setUserName(u);
		setProduct(p);
		setSide(s);
		setOrderId(id);
	}
	
	//Manipulation Methods
	private void setUserName(String u){
		if(u == null){
			throw new NullPointerException("Username parameter cannot be null");
		}
		userName = u;
	}
	private void setProduct(String p){
		if(p == null){
			throw new NullPointerException("Product parameter cannot be null");
		}
		product = p;
	}
	private void setSide(String s){
		if(s == null){
			throw new NullPointerException("Side parameter cannot be null");
		}
		side = s;
	}
	private void setOrderId(String o){
		if(o == null){
			throw new NullPointerException("OrderId parameter cannot be null");
		}
		orderId = o;
	}
	
	//Query Methods
	public String getUserName(){
		return userName;
	}
	public String getProduct(){
		return product;
	}
	public String getSide(){
		return side;
	}
	public String getOrderId(){
		return orderId;
	}
	public String toString(){
		return "User " + userName + ", " + side + product + "(" + orderId + ")"; 
	}
}
