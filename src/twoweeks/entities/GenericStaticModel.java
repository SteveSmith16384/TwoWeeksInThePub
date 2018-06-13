package twoweeks.entities;

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class GenericStaticModel extends PhysicalEntity {

	public GenericStaticModel(IEntityController _game, int id, int type, String name, String modelFile, float height, String tex, float x, float y, float z, Vector3f dir, boolean moveToFloor) {
		super(_game, id, type, name, false, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("name", name);
			creationData.put("modelFile", modelFile);
			creationData.put("height", height);
			creationData.put("tex", tex);
			creationData.put("dir", dir);
			creationData.put("moveToFloor", moveToFloor);
		}

		Spatial model = game.getAssetManager().loadModel(modelFile);
		if (tex != null) {
			JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), model, tex);
		}
		model.setShadowMode(ShadowMode.CastAndReceive);
		if (height > 0) {
			JMEModelFunctions.scaleModelToHeight(model, height);
		}

		if (moveToFloor) {
			JMEModelFunctions.moveYOriginTo(model, 0f);
		}

		this.mainNode.attachChild(model);
		
		mainNode.lookAt(this.getWorldTranslation().add(dir), Vector3f.UNIT_Y);//.setLocalRotation(q);
		
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(2);
		simpleRigidBody.setNeverMoves(true);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

	}


	@Override
	public Collidable getCollidable() {
		return this.mainNode;
	}


}
