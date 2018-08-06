package twoweeks.entities;

import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;

import twoweeks.TwoWeeksGlobals;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.CarAvatarModel;
import twoweeks.server.TwoWeeksServer;

public class PlayerCarServerAvatar extends AbstractServerAvatar implements IDebrisTexture {
	
	public PlayerCarServerAvatar(TwoWeeksServer _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, TwoWeeksClientEntityCreator.CAR_AVATAR, client, _input, eid, new CarAvatarModel(_module.getAssetManager()), 100f, TwoWeeksGlobals.PRI_PLAYER, null); // todo
	
	}

	
	@Override
	public String getDebrisTexture() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public float getMinDebrisSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMaxDebrisSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public void updateClientSideHealth(int amt) {
		// Do nothing
		
	}
	

}
