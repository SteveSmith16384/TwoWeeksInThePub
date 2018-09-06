package twoweeks.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.server.TwoWeeksServer;

public class PlayersBullet extends AbstractBullet implements INotifiedOfCollision { // todo - rename, also for AI

	public static final float RANGE = 30f;
	public static final float SPEED = 10f;

	public PlayersBullet(IEntityController _game, int id, int playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client) {
		super(_game, id, TwoWeeksClientEntityCreator.BULLET, "PlayersBullet", playerOwnerId, _shooter, startPos, _dir, _side, _client, true, SPEED, RANGE);

		//this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		Vector3f origin = Vector3f.ZERO;
		Spatial laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(TwoWeeksServer.LASER_LENGTH)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png", TwoWeeksServer.LASER_DIAM, true);
		laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);

	}


	@Override
	public float getDamageCaused() {
		float dist = Math.max(1, this.getDistanceTravelled());
		return ((RANGE-dist) / dist) * 1;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			// todo
			//ExplosionEffectEntity expl = new ExplosionEffectEntity(game, game.getNextEntityID(), this.getWorldTranslation());
			//game.addEntity(expl);

			if (Globals.SHOW_BULLET_COLLISION_POS) {
				// Create debugging sphere
				Vector3f pos = this.getWorldTranslation();
				DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), pos.x, pos.y, pos.z, true, false);
				game.addEntity(ds);
			}
		}
		//this.remove();
		game.markForRemoval(this.getID());
	}

}
