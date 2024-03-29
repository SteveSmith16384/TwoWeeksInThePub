package twoweeks.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;
import ssmith.util.RealtimeInterval;
import twoweeks.TwoWeeksCollisionValidator;
import twoweeks.TwoWeeksGameData;
import twoweeks.TwoWeeksGlobals;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.AbstractAISoldier;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.PlayerMercServerAvatar;
import twoweeks.netmessages.EnterCarMessage;
import twoweeks.netmessages.GameDataMessage;
import twoweeks.server.maps.CustomMap;
import twoweeks.server.maps.IMapCreator;

public class TwoWeeksServer extends AbstractGameServer implements ITerrainHeightAdjuster {

	public static final float LASER_DIAM = 0.03f;
	public static final float LASER_LENGTH = 0.7f;
	public static final float STEP_FORCE_ = 8f;
	public static final float RAMP_FORCE = 3f;

	private static AtomicInteger nextSideNum = new AtomicInteger(0);

	public static final String GAME_ID = "Two Weeks";

	private TwoWeeksCollisionValidator collisionValidator = new TwoWeeksCollisionValidator();
	private TwoWeeksGameData twipGameData;
	private IMapCreator mapCreator;
	private RealtimeInterval countUnitsInt;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("No config file specified.  Using defaults.");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost");
			int gamePort = props.getPropertyAsInt("gamePort", TwoWeeksGlobals.PORT);

			new TwoWeeksServer(gameIpAddress, gamePort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private TwoWeeksServer(String gameIpAddress, int gamePort) throws IOException {
		super(new ValidateClientSettings(GAME_ID, 1d, "key"), 
				new GameOptions(Globals.DEFAULT_TICKRATE, Globals.DEFAULT_SEND_UPDATES_INTERVAL, Globals.DEFAULT_RENDER_DELAY, Globals.DEFAULT_NETWORK_TIMEOUT,
				10*1000, 10*60*1000, 10*1000, 
				gameIpAddress, gamePort, 
				10, 5));

		this.mapCreator = new CustomMap(this);
		countUnitsInt = new RealtimeInterval(2000);

		super.physicsController.setStepForce(TwoWeeksGlobals.STEP_FORCE);
		super.physicsController.setRampForce(TwoWeeksGlobals.RAMP_FORCE);

		start(JmeContext.Type.Headless);

	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs);

		if (super.gameData.isInGame()) {
			if (countUnitsInt.hitInterval()) {
				twipGameData.numUnitsLeft = 0;
				for (int i=0 ; i<this.entitiesForProcessing.size() ; i++) {
					IEntity e = this.entitiesForProcessing.get(i);
					if (e instanceof AbstractAvatar || e instanceof AbstractAISoldier) {
						twipGameData.numUnitsLeft++;
					}
				}
				this.sendMessageToInGameClients(new GameDataMessage(this.twipGameData));
			}
		}
	}


	@Override
	protected void handleMessage(MyAbstractMessage message) {
		if (message instanceof EnterCarMessage) {
			this.playerEnterCar((EnterCarMessage)message);
		} else {
			super.handleMessage(message);
		}
	}
	

	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		Vector3f pos = this.mapCreator.getStartPos();
		avatar.setWorldTranslation(pos.x, pos.y, pos.z);

		if (Globals.DEBUG_PLAYER_START_POS) {
			Globals.p("Moving " + avatar + " to start pos: " + pos);
		}
	}


	@Override
	protected void createGame() {
		this.twipGameData = new TwoWeeksGameData();
		nextSideNum = new AtomicInteger(0); // Start side nums on 1 again
		this.mapCreator.createMap();
	}


	public GenericStaticModel getRandomVehicle(Vector3f pos) {
		float height = 0.7f;
		int i = NumberFunctions.rnd(1, 4);
		switch (i) {
		case 1:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BasicCar", "Models/Car pack by Quaternius/BasicCar.blend", height, "Models/Car pack by Quaternius/CarTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 2:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "CopCar", "Models/Car pack by Quaternius/CopCar.blend", height, "Models/Car pack by Quaternius/CopTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 3:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "RaceCar", "Models/Car pack by Quaternius/RaceCar.blend", height, "Models/Car pack by Quaternius/RaceCarTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 4:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Taxi", "Models/Car pack by Quaternius/Taxi.blend", height, "Models/Car pack by Quaternius/TaxiTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		default:
			throw new RuntimeException("Invalid number: " + i);
		}

	}


	public GenericStaticModel getRandomBuilding(Vector3f pos) {
		int i = NumberFunctions.rnd(1, 5);
		switch (i) {
		case 1:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BigBuilding", "Models/Suburban pack Vol.2 by Quaternius/Blends/BigBuilding.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/BigBuildingTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 2:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BurgerShop", "Models/Suburban pack Vol.2 by Quaternius/Blends/BurgerShop.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/BurgerShopTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 3:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Shop", "Models/Suburban pack Vol.2 by Quaternius/Blends/Shop.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/ShopTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 4:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "SimpleHouse", "Models/Suburban pack Vol.2 by Quaternius/Blends/SimpleHouse.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/HouseTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		case 5:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "SimpleHouse2", "Models/Suburban pack Vol.2 by Quaternius/Blends/SimpleHouse2.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/HouseTexture.png", pos.x, pos.y, pos.z, new Vector3f(), true, 1f);
		default:
			throw new RuntimeException("Invalid number: " + i);
		}

	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		PlayerMercServerAvatar avatar = new PlayerMercServerAvatar(this, client, client.remoteInput, entityid);
		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		if (pa.type != TwoWeeksClientEntityCreator.TERRAIN1 && pb.type != TwoWeeksClientEntityCreator.TERRAIN1) {
			//Globals.p("Collision between " + pa + " and " + pb);
		}

		super.collisionOccurred(a, b);

	}


	@Override
	protected byte getWinningSideAtEnd() {
		return -1; // todo
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return this.collisionValidator.canCollide(a, b);
	}


	@Override
	protected Class<? extends Object>[] getListofMessageClasses() {
		return new Class[] {TwoWeeksGameData.class, GameDataMessage.class, EnterCarMessage.class}; // Must be in the same order on client and server!
	}


	@Override
	public byte getSideForPlayer(ClientData client) {
		byte side = (byte)nextSideNum.getAndAdd(1);
		if (side < 0) {
			throw new RuntimeException("Too many sides!");
		}
		return side;

	}


	@Override
	public boolean doWeHaveSpaces() {
		return true; // todo - not if game started?
	}


	@Override
	public void playerKilled(AbstractServerAvatar avatar) {
		super.playerKilled(avatar);

		checkForWinner();
	}


	private void checkForWinner() {
		// todo
	}


	@Override
	public int getMinSidesRequiredForGame() {
		return 1;
	}


	@Override
	public void adjustHeight(AbstractHeightMap heightmap) {
		/*float h = heightmap.getInterpolatedHeight(CITY_X, CITY_Z); 
		for (int z=CITY_Z ; z<CITY_Z+CITY_SIZE ; z++) {
			for (int x=CITY_X ; x<CITY_X+CITY_SIZE ; x++) {
				//Globals.p("x=" + x + ", z=" + z);
				heightmap.setHeightAtPoint(h, x, z);
			}			
		}*/
		if (this.mapCreator instanceof ITerrainHeightAdjuster) {
			ITerrainHeightAdjuster tha = (ITerrainHeightAdjuster)this.mapCreator;
			tha.adjustHeight(heightmap);
		}

	}


	private void playerEnterCar(EnterCarMessage msg) {
		//1 - find car
		//2 - change avatar to car
	}
}
