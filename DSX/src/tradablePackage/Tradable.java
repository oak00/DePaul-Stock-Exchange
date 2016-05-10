package tradablePackage;

import exceptions.InvalidPriceOperation;
import exceptions.InvalidVolumeOperation;
import pricePackage.Price;
import pricePackage.PriceFactory;

public interface Tradable {

	String getProduct();
	Price getPrice();
	int getOriginalVolume();
	int getRemainingVolume();
	int getCancelledVolume();
	void setCancelledVolume(int newCancelledVolume) throws InvalidVolumeOperation;
	void setRemainingVolume(int newRemainingVolume) throws InvalidVolumeOperation;
	String getUser();
	String getSide();
	boolean isQuote();
	String getId();
	
}
