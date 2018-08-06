package twoweeks.entities;

import com.jme3.renderer.Camera;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.CarAvatarModel;

public class PlayerCarClientAvatar extends AbstractClientAvatar {
	
	public PlayerCarClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, IHUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, TwoWeeksClientEntityCreator.CAR_AVATAR, _playerID, _input, _cam, _hud, eid, x, y, z, side, new CarAvatarModel(_module.getAssetManager()), null); // todo
		
		//this.playerGeometry.setShadowMode(ShadowMode.Off);

	}

}