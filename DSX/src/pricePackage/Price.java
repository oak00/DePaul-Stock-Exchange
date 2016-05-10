package pricePackage;

import exceptions.InvalidPriceOperation;

public class Price implements Comparable<Price> {

		private long amount;
		private boolean MKT;
		
		Price(long value){
			amount = value;
			MKT = false;
		}
		
		Price(){
			MKT = true;
		}
		
		private boolean getMarketPrice(){
			return MKT;
		}
		
		public Price add(Price p) throws InvalidPriceOperation {
			if(isMarket()){
				throw new InvalidPriceOperation("Cannot add a LIMIT price to a MARKET price.");
			}
			if(p.isMarket()){
				throw new InvalidPriceOperation("Cannot add a MARKET price to a LIMIT price.");
			}
			
			long sum = amount + p.getValue();
			return PriceFactory.makeLimitPrice(sum);
		}
		
		public Price subtract(Price p) throws InvalidPriceOperation {
			if(isMarket()){
				throw new InvalidPriceOperation("Cannot subtract a LIMIT price from a MARKET price.");
			}
			if(p.isMarket()){
				throw new InvalidPriceOperation("Cannot subtract a MARKET price from a LIMIT price.");
			}
			
			long dif = amount - p.getValue();
			return PriceFactory.makeLimitPrice(dif);
		}
		
		public Price multiply(int p) throws InvalidPriceOperation {
			if(isMarket()){
				throw new InvalidPriceOperation("Cannot multiply a MARKET price.");
			}
			long product = amount*p;
			return PriceFactory.makeLimitPrice(product);
		}
		
		public int compareTo(Price p){
			if(amount == p.getValue()){
				return 0;
			}
			else if( amount < p.getValue()){
				return -1;
			}
			else{
				return 1;
			}
		}
		
		public boolean greaterOrEqual(Price p){
			if(amount >= p.getValue()){
				return true;
			}
			return false;
		}
		
		public boolean greaterThan(Price p){
			if(amount > p.getValue()){
				return true;
			}
			return false;
		}
		
		public boolean lessOrEqual(Price p){
			if(amount <= p.getValue()){
				return true;
			}
			return false;
		}
		
		public boolean lessThan(Price p){
			if(amount < p.getValue()){
				return true;
			}
			return false;
		}
		
		public boolean equals(Price p){
			if(amount == p.getValue()){
				return true;
			}
			return false;
		}
		
		public boolean isMarket(){
			if(getMarketPrice() == true){
				return true;
			}
			return false;
		}
		
		public boolean isNegative(){
			if(amount < 0){
				return true;
			}
			return false;
		}
		
		public long getValue(){
			return amount;
		}
		
		public String toString(){
			if(isMarket()){
				return "MKT";
			}
			else{
				double l = (double) amount;
				l = l/100;
				return String.format("$%,.2f",l);
			}
		}
}
