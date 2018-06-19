package twoweeks;

import com.jme3.network.serializing.Serializable;

@Serializable
public class TwoWeeksGameData {

	public int numUnitsLeft;
	
	public TwoWeeksGameData() { // For Kryo
		super();
	}


}
