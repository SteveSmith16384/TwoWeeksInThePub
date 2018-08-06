package twoweeks.entities;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.models.SoldierModel;

public class MercEnemyAvatar extends AbstractEnemyAvatar implements AnimEventListener {
	
	//private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SoldierModel soldier;
	private int currentAnimCode = -1;
	
	public MercEnemyAvatar(IEntityController game, int type, int eid, float x, float y, float z, int side, String name) {
		super(game, type, eid, x, y, z, new SoldierModel(game.getAssetManager()), side, name);
		
		this.soldier = (SoldierModel)anim;
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
