package book;

import java.util.HashMap;

import exceptions.InvalidVolumeOperation;
import messages.FillMessage;
import tradablePackage.Tradable;

public interface TradeProcessor {

	public HashMap<String, FillMessage> doTrade(Tradable trd) throws InvalidVolumeOperation;
}
