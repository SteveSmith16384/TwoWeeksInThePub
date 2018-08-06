package twoweeks.entities;

import com.jme3.renderer.Camera;
import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;

import twoweeks.TwoWeeksGlobals;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierModel;

public class PlayerMercClientAvatar extends AbstractClientAvatar {
	
	public PlayerMercClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, IHUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, TwoWeeksClientEntityCreator.SOLDIER_AVATAR, _playerID, _input, _cam, _hud, eid, x, y, z, side, new SoldierModel(_module.getAssetManager()), new PersonAvatar(_module, _input, TwoWeeksGlobals.MOVE_SPEED, TwoWeeksGlobals.JUMP_FORCE));
		
		//this.playerGeometry.setShadowMode(ShadowMode.Off);

	}

}