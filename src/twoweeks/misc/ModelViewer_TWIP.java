package twoweeks.misc;

import com.jme3.scene.Spatial;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.misc.ModelViewer;

public class ModelViewer_TWIP extends ModelViewer {

	public static void main(String[] args) {
		ModelViewer_TWIP app = new ModelViewer_TWIP();
		app.showSettings = false;

		app.start();
	}


	@Override
	public Spatial getModel() {
		Spatial model = assetManager.loadModel("Models/redeclipse/RE_Player_male-2.blend");
		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, SoldierTexture.getTexture(false, false));

		JMEModelFunctions.scaleModelToHeight(model, .7f);
		JMEModelFunctions.moveYOriginTo(model, 0f);

		return model;
	}
	
	
	@Override
	public String getAnimNode() {
		return "erebus.001 (Node)";
	}
	

	@Override
	public String getAnimToShow() {
		return "run";
	}


}
