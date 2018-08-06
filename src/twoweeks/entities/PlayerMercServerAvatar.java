package twoweeks.entities;

import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;

import twoweeks.TwoWeeksGlobals;
import twoweeks.abilities.PlayersMachineGun;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierModel;
import twoweeks.server.TwoWeeksServer;

public class PlayerMercServerAvatar extends AbstractServerAvatar implements IDebrisTexture {
	
	public PlayerMercServerAvatar(TwoWeeksServer _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, TwoWeeksClientEntityCreator.SOLDIER_AVATAR, client, _input, eid, new SoldierModel(_module.getAssetManager()), 100f, TwoWeeksGlobals.PRI_PLAYER, new PersonAvatar(_module, _input, TwoWeeksGlobals.MOVE_SPEED, TwoWeeksGlobals.JUMP_FORCE));
		
		IAbility abilityGun = new PlayersMachineGun(_module, _module.getNextEntityID(), playerID, this, eid, 0, client);
		_module.actuallyAddEntity(abilityGun);
		
		//IAbility abilityGrenades = new GrenadeLauncher(_module, _module.getNextEntityID(), this, 1, client);
		//_module.actuallyAddEntity(abilityGrenades);

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
