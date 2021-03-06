package twoweeks.entities;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.models.SoldierAvatarModel;

public class MercEnemyAvatar extends AbstractOtherPlayersAvatar implements AnimEventListener {
	
	//private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SoldierAvatarModel soldier;
	private int currentAnimCode = -1;
	
	public MercEnemyAvatar(IEntityController game, int type, int eid, float x, float y, float z, byte side, String name) {
		super(game, type, eid, x, y, z, new SoldierAvatarModel(game.getAssetManager()), side, name);
		
		this.soldier = (SoldierAvatarModel)model;
	}
	

	@Override
	public void setAnimCode_ClientSide(int animCode) {
		//Globals.p("SoldierEnemyAvatar: setCurrentAnimForCode(" + s + ")");
		if (animCode != this.currentAnimCode) {
			soldier.setAnim(animCode);
		}
		this.currentAnimCode = animCode;
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// Do nothing, JME handles it
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Jump")) {
			soldier.isJumping = false;
			this.currentAnimCode = AbstractAvatar.ANIM_IDLE;
		}
	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// Do nothing
	}


}
