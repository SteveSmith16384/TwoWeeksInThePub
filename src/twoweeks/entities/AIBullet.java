package twoweeks.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.server.TwoWeeksServer;

public class AIBullet extends AbstractAIBullet implements INotifiedOfCollision {

	public static final float RANGE = 25f;
	public static final float SPEED = 18f;
	private static final boolean USE_CYLINDER = true;

	public AIBullet(IEntityController _game, int id, int side, float x, float y, float z, IEntity shooter, Vector3f dir) {
		super(_game, id, TwoWeeksClientEntityCreator.AI_BULLET, x, y, z, "AIBullet", side, shooter, dir, true, SPEED, RANGE);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("shooterID", shooter.getID());
			creationData.put("dir", dir);
		}

		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createBulletModel(Vector3f dir) {
		Spatial laserNode = null;
		if (USE_CYLINDER) {
			Vector3f origin = Vector3f.ZERO;
			laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(TwoWeeksServer.LASER_LENGTH)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png", TwoWeeksServer.LASER_DIAM, true);
		} else {
			Mesh sphere = new Sphere(8, 8, .02f, true, false);
			laserNode = new Geometry("DebuggingSphere", sphere);
			TextureKey key3 = new TextureKey("Textures/bullet1.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			mat.setTexture("DiffuseMap", tex3);
			laserNode.setMaterial(mat);
		}

		laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);

	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			AbstractGameServer server = (AbstractGameServer)game;
			String tex = "Textures/sun.jpg";
			if (pe instanceof IDebrisTexture) {
				IDebrisTexture dt = (IDebrisTexture)pe;
				tex = dt.getDebrisTexture();
			}
			//server.sendExplosion(this.getWorldTranslation(), 4, .8f, 1.2f, .04f, .1f, tex);  No, too many entities!
		}
		this.remove();
	}


}
