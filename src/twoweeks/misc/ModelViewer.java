package twoweeks.misc;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

import twoweeks.client.SoldierTexture;

public class ModelViewer extends SimpleApplication implements AnimEventListener {

	private AnimControl control;

	public static void main(String[] args) {
		ModelViewer app = new ModelViewer();
		app.showSettings = false;

		app.start();
	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default

		super.getViewPort().setBackgroundColor(ColorRGBA.Black);

		cam.setFrustumPerspective(60, settings.getWidth() / settings.getHeight(), .1f, 100);

		setupLight();

		//Spatial model = assetManager.loadModel("Models/Airplane pack by Quaternius/OBJ/Military Airplane.obj");
		Spatial model = assetManager.loadModel("Models/Airplane pack by Quaternius/Blends/Private plane.blend");
		
		//Spatial model = assetManager.loadModel("Models/AnimatedHuman/Animated Human.blend");
		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, SoldierTexture.getTexture());

		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/landscape_asset_v2a/obj/basetexture.jpg");
		rootNode.attachChild(model);

		if (model instanceof Node) {
			control = this.getNodeWithControls((Node)model);
			if (control != null) {
				control.addListener(this);
				Globals.p("Animations: " + control.getAnimationNames());
				AnimChannel channel = control.createChannel();
				//channel.setAnim("run");
			} else {
				Globals.p("No animation control");
			}
		}


		this.rootNode.attachChild(JMEModelFunctions.getGrid(assetManager, 10));

		rootNode.updateGeometricState();

		model.updateModelBound();
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		Globals.p("Model w/h/d: " + (bb.getXExtent()*2) + "/" + (bb.getYExtent()*2) + "/" + (bb.getZExtent()*2));

		this.flyCam.setMoveSpeed(12f);

	}


	private AnimControl getNodeWithControls(Node s) {
		int ch = s.getChildren().size();
		for (int i=0 ; i<ch ; i++) {
			Spatial sp = s.getChild(i);
			if (sp instanceof Node) {
				Node n2 = (Node)sp;
				if (n2.getNumControls() > 0) {
					control = n2.getControl(AnimControl.class);
					if (control != null) {
						return control;
					}
				} else {
					return this.getNodeWithControls((Node)sp);
				}
			}
		}
		return null;
	}


	private void setupLight() {
		// Remove existing lights
		this.rootNode.getWorldLightList().clear();
		LightList list = this.rootNode.getWorldLightList();
		for (Light it : list) {
			this.rootNode.removeLight(it);
		}

		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1));
		rootNode.addLight(al);

		DirectionalLight dirlight = new DirectionalLight(); // FSR need this for textures to show
		dirlight.setColor(ColorRGBA.White.mult(1.5f));
		rootNode.addLight(dirlight);

	}


	@Override
	public void simpleUpdate(float tpf) {
		//System.out.println("Pos: " + this.cam.getLocation());
		//this.rootNode.rotate(0,  tpf,  tpf);

		//Globals.p("Model w/h/d: " + (bb.getXExtent()*2) + "/" + (bb.getYExtent()*2) + "/" + (bb.getZExtent()*2));
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

	}


}