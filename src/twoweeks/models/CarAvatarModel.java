package twoweeks.models;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

public class CarAvatarModel implements IAvatarModel {

	public static final float MODEL_HEIGHT = 0.7f;
	private static final float MODEL_WIDTH = 0.3f; // todo - check these
	private static final float MODEL_DEPTH = 0.3f;

	private AssetManager assetManager;
	private Spatial model;

	public CarAvatarModel(AssetManager _assetManager) {
		assetManager = _assetManager;
	}


	@Override
	public Spatial createAndGetModel() {
		if (!Globals.USE_BOXES_FOR_AVATARS_SOLDIER) {
			model = assetManager.loadModel("Models/Car pack by Quaternius/RaceCar.blend");
			JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/Car pack by Quaternius/RaceCarTexture.png");
			JMEModelFunctions.scaleModelToHeight(model, MODEL_HEIGHT);
			JMEModelFunctions.moveYOriginTo(model, 0f);
		} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			model = new Geometry("Soldier", box1);
			model.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor
			JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Textures/greensun.jpg");
		}
		model.setShadowMode(ShadowMode.CastAndReceive);
		return model;
	}


	@Override
	public float getCameraHeight() {
		return MODEL_HEIGHT - 0.2f;
	}


	@Override
	public float getBulletStartHeight() {
		return MODEL_HEIGHT - 0.3f;
	}


	public void setAnim(int animCode) {

	}


	public int getCurrentAnimCode() {
		return -1;
	}


	@Override
	public Vector3f getCollisionBoxSize() {
		return new Vector3f(MODEL_WIDTH, MODEL_HEIGHT, MODEL_DEPTH);
	}


	@Override
	public Spatial getModel() {
		return model;
	}

}
