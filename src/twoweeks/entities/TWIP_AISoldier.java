package twoweeks.entities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierAvatarModel;
import twoweeks.server.ai.TWIPSoldierAI3;

public class TWIP_AISoldier extends AbstractAISoldier {

	public TWIP_AISoldier(IEntityController _game, int id, float x, float y, float z, int csInitialAnimCode, String name) {
		super(_game, id, TwoWeeksClientEntityCreator.AI_SOLDIER, x, y, z, (byte)id, 
				new SoldierAvatarModel(_game.getAssetManager()), csInitialAnimCode, name);

		if (_game.isServer()) {
			ai = new TWIPSoldierAI3(this);
		}
	}

	
	@Override
	protected AbstractBullet createBullet(Vector3f pos, Vector3f dir) {
		PlayersBullet bullet = new PlayersBullet(game, game.getNextEntityID(), -1, this, pos, dir,  side, null);
		return bullet;
	}


	@Override
	public void updateClientSideHealth(int amt) {
		
	}
	

}

