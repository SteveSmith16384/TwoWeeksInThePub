package twoweeks.entities;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.models.CarAvatarModel;
import twoweeks.models.SoldierAvatarModel;

public class CarEnemyAvatar extends AbstractOtherPlayersAvatar implements AnimEventListener {
	
	public CarEnemyAvatar(IEntityController game, int type, int eid, float x, float y, float z, int side, String name) {
		super(game, type, eid, x, y, z, new CarAvatarModel(game.getAssetManager()), side, name);
	}
	

	@Override
	public void setAnimCode_ClientSide(int animCode) {
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// Do nothing, JME handles it
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// Do nothing
	}


}
