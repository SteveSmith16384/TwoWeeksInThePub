package twoweeks.entities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierAvatarModel;
import twoweeks.server.ai.TWIPSoldierAI3;

public class TWIP_AISoldier extends AbstractAISoldier {

	public TWIP_AISoldier(IEntityController _game, int id, float x, float y, float z, int _side, int csInitialAnimCode, String name) {
		super(_game, id, TwoWeeksClientEntityCreator.AI_SOLDIER, x, y, z, _side, 
				new SoldierAvatarModel(_game.getAssetManager()), csInitialAnimCode, name);

		if (_game.isServer()) {
			ai = new TWIPSoldierAI3(this);
		}
	}

	
	@Override
	protected AbstractAIBullet createBullet(Vector3f pos, Vector3f dir) {
		AIBullet bullet = new AIBullet(game, game.getNextEntityID(), side, pos.x, pos.y, pos.z, this, dir);
		return bullet;
	}


	@Override
	public void updateClientSideHealth(int amt) {
		
	}
	

}

