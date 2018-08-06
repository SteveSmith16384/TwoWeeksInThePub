package twoweeks.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class EnterCarMessage extends MyAbstractMessage {
	
	public boolean enter; // or exit
	
	public EnterCarMessage() {
		super(true, true);
	}
	
	
	public EnterCarMessage(boolean b) {
		this();
				
		this.enter = b;
	}
}
