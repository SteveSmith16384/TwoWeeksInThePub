package twoweeks.misc;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.simplephysics.tests.SimpleEntityHelper;
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

		//Spatial model = assetManager.loadModel("Models/AnimatedHuman/Animated Human.blend");
		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, SoldierTexture.getTexture());

		//Spatial model = assetManager.loadModel("Models/Suburban pack Vol.2 by Quaternius/Blends/BigBuilding.blend");
		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/BigBuildingTexture.png");
		
		Spatial model = assetManager.loadModel("Models/landscape_asset_v2a/obj/grass.obj");
		//Spatial model = assetManager.loadModel("Models/landscape_asset_v2a/obj/hill-ramp.obj");
		JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/landscape_asset_v2a/obj/basetexture.jpg");

		rootNode.attachChild(model);

		if (model instanceof Node) {  // model.getWorldBound()
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


		// Add grid
		this.rootNode.attachChild(JMEModelFunctions.getGrid(assetManager, 10));
		
		// Add centre box
		this.rootNode.attachChild(getBox(0, 0, 0, .1f, 10f, .1f));
		
		rootNode.updateGeometricState();

		model.updateModelBound();
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		Globals.p("Model centre: " + bb.getCenter());
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


	private Geometry getBox(float x, float y, float z, float w, float h, float d) {
		Box box = new Box(w/2, h/2, d/2);

		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/crate.png");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		material.setTexture("ColorMap", tex3);

		final Geometry boxGeometry = new Geometry("Box", box);
		boxGeometry.setMaterial(material);
		boxGeometry.setLocalTranslation(x, y, z);
		
		return boxGeometry;
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