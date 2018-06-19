package twoweeks.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

import twoweeks.TwoWeeksGameData;

@Serializable
public class GameDataMessage extends MyAbstractMessage {

	public TwoWeeksGameData gameData;
	
	public GameDataMessage() {
		// Serialzation
	}


	public GameDataMessage(TwoWeeksGameData _gameData) {
		this();
		
		gameData = _gameData;
	}
}
