package twoweeks.client;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

import ssmith.util.MyProperties;
import twoweeks.TwoWeeksCollisionValidator;
import twoweeks.TwoWeeksGameData;
import twoweeks.TwoWeeksGlobals;
import twoweeks.client.hud.TwoWeeksHUD;
import twoweeks.netmessages.EnterCarMessage;
import twoweeks.netmessages.GameDataMessage;
import twoweeks.server.TwoWeeksServer;

public class TwoWeeksClient extends AbstractGameClient {

	private TwoWeeksClientEntityCreator entityCreator;
	private DirectionalLight sun;
	private TwoWeeksCollisionValidator collisionValidator;
	private TwoWeeksHUD hud;

	private String ipAddress;
	private int port;

	private FilterPostProcessor FPPWater;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("Warning: No config file specified");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost");
			int gamePort = props.getPropertyAsInt("gamePort", TwoWeeksGlobals.PORT);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);

			float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);

			new TwoWeeksClient(gameIpAddress, gamePort,
					tickrateMillis, clientRenderDelayMillis, timeoutMillis,
					mouseSensitivity);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	private TwoWeeksClient(String gameIpAddress, int gamePort,
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis,
			float mouseSensitivity) {
		super(new ValidateClientSettings(TwoWeeksServer.GAME_ID, 1, "key"), "Two Weeks", null,  
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, mouseSensitivity);

		ipAddress = gameIpAddress;
		port = gamePort;
		start();
	}


	@Override
	public void simpleInitApp() {
		super.physicsController.setStepForce(TwoWeeksGlobals.STEP_FORCE);
		super.physicsController.setRampForce(TwoWeeksGlobals.RAMP_FORCE);

		hud = new TwoWeeksHUD(this, this.cam);
		this.getGuiNode().attachChild(hud);

		super.simpleInitApp();

		collisionValidator = new TwoWeeksCollisionValidator();
		entityCreator = new TwoWeeksClientEntityCreator();

		this.getViewPort().setBackgroundColor(ColorRGBA.Cyan);

		{
			// Add shadows
			final int SHADOWMAP_SIZE = 1024*2;
			DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
			dlsr.setLight(sun);
			this.viewPort.addProcessor(dlsr);
		}

		{
			// DepthOfFieldFilter
			FilterPostProcessor fpp2 = new FilterPostProcessor(getAssetManager());
			DepthOfFieldFilter dff = new DepthOfFieldFilter();
			dff.setFocusDistance(10f);
			dff.setFocusRange(40f);
			fpp2.addFilter(dff);
			viewPort.addProcessor(fpp2);
		}
/*
		{
			// Bloom
			BloomFilter bloom = new BloomFilter();
			bloom.setDownSamplingFactor(2);
			bloom.setBlurScale(1.37f);
			bloom.setExposurePower(3.30f);
			bloom.setExposureCutOff(0.2f);
			bloom.setBloomIntensity(2.45f);
			FilterPostProcessor fpp2 = new FilterPostProcessor(getAssetManager());
			fpp2.addFilter(bloom);
			viewPort.addProcessor(fpp2);
		}
*/

		this.connect(ipAddress, port, false);
	}


	@Override
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.6f));
		getGameNode().addLight(al);

		sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.4f, -.8f, .4f).normalizeLocal());
		getGameNode().addLight(sun);
	}


	@Override
	protected void allEntitiesReceived() {
		super.allEntitiesReceived();
		getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

		{
			if (FPPWater != null) {
				viewPort.removeProcessor(FPPWater);
			}
			// Water
			FPPWater = new FilterPostProcessor(getAssetManager());
			WaterFilter water = new WaterFilter(getRootNode(), sun.getDirection());
			water.setWaterHeight(0);
			FPPWater.addFilter(water);
			viewPort.addProcessor(FPPWater);
		}

	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		super.simpleUpdate(tpfSecs);
		this.hud.processByClient(this, tpfSecs);
	}


	@Override
	protected boolean handleMessage(MyAbstractMessage message) {
		if (message instanceof GameDataMessage) {
			GameDataMessage hdm = (GameDataMessage) message;
			this.hud.setUnitsRemaining(hdm.gameData.numUnitsLeft);
			return true;
		} else {
			return super.handleMessage(message);
		}
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return this.collisionValidator.canCollide(a, b);
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		//PhysicalEntity pea = a.userObject;
		//PhysicalEntity peb = b.userObject;

		super.collisionOccurred(a, b);

	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		return entityCreator.createEntity(client, msg);
	}

	/*
	@Override
	protected void playerHasWon() {
		removeCurrentHUDTextImage();
		//currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/victory.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDTextImage();
		//currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDTextImage();
		//currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}
	 */


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDTextImage();
			//currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/waitingforplayers.png", width, height, 5);
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDTextImage();
			//currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/getready.png", width, height, 5);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDTextImage();
			//currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/missionstarted.png", width, height, 5);
			break;
		case SimpleGameData.ST_FINISHED:
			// Don't show anything, this will be handled with a win/lose message
			break;
		default:
			// DO nothing
		}

	}


	private void removeCurrentHUDTextImage() {
		/*if (this.currentHUDTextImage != null) {
			if (currentHUDTextImage.getParent() != null) {
				currentHUDTextImage.remove();
			}
		}*/
	}


	@Override
	protected Class<? extends Object>[] getListofMessageClasses() {
		return new Class[] {TwoWeeksGameData.class, GameDataMessage.class, EnterCarMessage.class}; // Must be in the same order on client and server!
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase(TEST)) {
			if (value) {
				super.sendMessage(new EnterCarMessage(true));
			}
		} else {
			super.onAction(name, value, tpf);
		}
	}


	@Override
	protected void showDamageBox() {
		hud.showDamageBox();
	}


	@Override
	protected void showMessage(String msg) {
		hud.appendToLog(msg);
	}


	@Override
	protected void appendToLog(String msg) {
		hud.appendToLog(msg);
	}


	@Override
	public void runWhenDisconnected() {
		this.showMessage("DISCONNECTED");
	}

}
