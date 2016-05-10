package messages;

import exceptions.InvalidMessageState;

public class MarketMessage {

	private String state;
	
	public MarketMessage(String s) throws InvalidMessageState{
		this.setState(s);
	}
	
	public String getMarketMessage(){
		return state;
	}
	
	private void setState(String s) throws InvalidMessageState{
		
		if((!s.equals("CLOSED")) && (!s.equals("PREOPEN")) && (!s.equals("OPEN"))){
			throw new InvalidMessageState("Invalid Market State");
		}
		state = s;
	}
	
	public String getState(){
		return state;
	}
}
