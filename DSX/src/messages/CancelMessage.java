package messages;

import pricePackage.*;

public class CancelMessage implements Comparable<CancelMessage> {

	private String user;
	private String product;
	private Price price;
	private int volume;
	private String details;
	private String side;
	public String id;
	
	public CancelMessage(String u, String p, Price pr, int v,
						 String d, String s, String i){

		this.setUser(u);
		this.setProduct(p);
		this.setPrice(pr);
		this.setVolume(v);
		this.setDetails(d);
		this.setSide(s);
		this.setId(i);
	}
	
	private void setUser(String u){
		user = u;
	}
	private void setProduct(String p){
		product = p;		
	}
	private void setPrice(Price pr){
		price = pr;		
	}
	private void setVolume(int v){
		volume = v;
	}
	private void setDetails(String d){
		details = d;
	}
	private void setSide(String s){
		side = s;
	}
	private void setId(String i){
		id = i;
	}
	public String getUser(){
		return user;
	}
	public String getProduct(){
		return product;
	}
	public Price getPrice(){
		return price;
	}
	public int getVolume(){
		return volume;
	}
	public String getDetails(){
		return details;
	}
	public String getSide(){
		return side;
	}
	public String getId(){
		return id;
	}
	public int compareTo(CancelMessage x){
		if(this.getPrice().equals(x.getPrice())){
			return 0;
		}
		else if (this.getPrice().greaterThan(x.getPrice())){
			return 1;
		}
		else{
			return -1;
		}
	}
	public String toString(){
		return "User: " + user + ", Product: " + product + ", Price: "
				+ price + ", Volume: " + volume + ", Details: " +
				details + ", Side: " + side + ", ID: " + id;
	}
}
