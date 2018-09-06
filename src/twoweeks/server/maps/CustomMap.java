package twoweeks.server.maps;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.Floor;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MapBorder;
import twoweeks.entities.TWIP_AISoldier;
import twoweeks.server.TwoWeeksServer;

public class CustomMap implements IMapCreator {

	private static final int NUM_AI_SOLDIERS = -1;//50; // -1 = Dont use this config
	private static final float AI_SOLDIERS_PER_SECTOR = 0.05f;// .0008f;
	
	// Map codes
	private static final int GRASS = 1;
	private static final int STRAIGHT_ROAD_LR_LOW = 2;
	private static final int STRAIGHT_ROAD_UD_LOW = 3;
	private static final int BEACH_TOP = 4;
	private static final int ROAD_BEND_RD_LOW = 5;
	private static final int ROAD_BEND_LD_LOW = 6;
	private static final int ROAD_BEND_LU_LOW = 7;
	private static final int ROAD_BEND_RU_LOW = 8;
	private static final int HOUSE = 9;
	private static final int ROAD_TJUNC_D_LOW = 10;
	private static final int ROAD_TJUNC_U_LOW = 11;
	private static final int ROAD_TJUNC_L_LOW = 12;
	private static final int ROAD_TJUNC_R_LOW = 13;
	private static final int WATER = 14;
	private static final int BEACH_BOTTOM = 15;
	private static final int ROAD_CROSSROADS_LOW = 16;

	private static final int HILL_RAMP_E_UP = 17;
	private static final int HILL_RAMP_W_UP = 18;
	private static final int HILL_RAMP_S_UP = 19;
	private static final int HILL_RAMP_N_UP = 20;

	private static final int ROAD_HILL_E_UP = 21; // todo - up or down?
	private static final int ROAD_HILL_W_UP = 22;
	private static final int ROAD_HILL_N_UP = 23;
	private static final int ROAD_HILL_S_UP = 24;

	private static final int CORNER_HILL_UP_NE = 25; // High ones
	private static final int CORNER_HILL_UP_NW = 26;
	private static final int CORNER_HILL_UP_SE = 27;
	private static final int CORNER_HILL_UP_SW = 28;

	private static final int CORNER_HILL_LOW_DOWN_NW = 29; // Low ones
	private static final int CORNER_HILL_LOW_DOWN_NE = 30;
	private static final int CORNER_HILL_LOW_DOWN_SW = 31;
	private static final int CORNER_HILL_LOW_DOWN_SE = 32;

	private static final int CLIFF = 33;

	private static final float ORIGINAL_SECTOR_SIZE = 8; // Actual size of the map tiles
	private static final float NEW_SECTOR_SIZE = 5; // The size we want to scale the tiles to
	private static float SECTOR_HEIGHT = 2 * (NEW_SECTOR_SIZE / ORIGINAL_SECTOR_SIZE);

	private TwoWeeksServer server;

	private int[][] map, mapHeight;


	public CustomMap(TwoWeeksServer _server) {
		server = _server;	
	}



	@Override
	public Vector3f getStartPos() {
		if (Globals.PLAYERS_START_IN_CORNER) {
			return new Vector3f(0, 7f, 0);
		} else {
			// Don't forget, centre of tiles is 0, 0, so edge of tile goes into negative space
			float x = NumberFunctions.rndFloat(0, (map.length-1) * NEW_SECTOR_SIZE);
			float z = NumberFunctions.rndFloat(0, (map[0].length-1) * NEW_SECTOR_SIZE);
			return new Vector3f(x, Globals.DEBUG_PLAYER_START_POS?3f:20f, z);
		}
	}


	@Override
	public void createMap() {		
		try {
			Globals.p("Loading map from file...");
			//String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/1x1_map.csv").toURI())));
			//String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/all_hills_test.csv").toURI())));
			//String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/test_map.csv").toURI())));
			String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/all_roads.csv").toURI())));
			//String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/large_map1.csv").toURI())));
			String[] lines = text.split(System.lineSeparator());

			map = new int[lines[0].split(",").length][lines.length];
			mapHeight = new int[lines[0].split(",").length][lines.length];

			for (int lineNum=0 ; lineNum<lines.length ; lineNum++) {
				String line = lines[lineNum];
				String[] tokens = line.split(",");
				for (int x=0 ; x<tokens.length ; x++) {
					mapHeight[x][lineNum] = 0;
					String code = null;
					if (tokens[x].contains(":")) {
						String subtokens[] = tokens[x].split(":");
						code = subtokens[0];
						mapHeight[x][lineNum] = Integer.parseInt(subtokens[1].trim());
					} else {
						code = tokens[x].trim();
					}
					map[x][lineNum] = Integer.parseInt(code.trim());
				}
			}

			for (int z=0 ; z<map[0].length ; z++) {
				for (int x=0 ; x<map.length ; x++) {
					GenericStaticModel model = getModel(map[x][z]);
					if (model != null) {
						float height = mapHeight[x][z] * SECTOR_HEIGHT;
						placeGenericModel(model, x*NEW_SECTOR_SIZE, height, z*NEW_SECTOR_SIZE);

						if (map[x][z] == HOUSE) {
							GenericStaticModel house = server.getRandomBuilding(new Vector3f(0, 0, 0));
							placeGenericModel(house, (x*NEW_SECTOR_SIZE), 2f, (z*NEW_SECTOR_SIZE));
							server.moveEntityUntilItHitsSomething(house, new Vector3f(0, -1, 0));
						} else if (map[x][z] == STRAIGHT_ROAD_UD_LOW) {
							GenericStaticModel car = server.getRandomVehicle(new Vector3f(0, 0, 0));
							placeGenericModel(car, x*NEW_SECTOR_SIZE, 2f, z*NEW_SECTOR_SIZE);
							server.moveEntityUntilItHitsSomething(car, new Vector3f(0, -1, 0));
						} else if (map[x][z] == GRASS ||
								map[x][z] == CORNER_HILL_LOW_DOWN_NE ||
								map[x][z] == CORNER_HILL_LOW_DOWN_NW ||
								map[x][z] == CORNER_HILL_LOW_DOWN_SE ||
								map[x][z] == CORNER_HILL_LOW_DOWN_SW) {
							GenericStaticModel tree = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/BigTreeWithLeaves.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", x, 0, z, JMEAngleFunctions.getRandomDirection_All(), true, 1f);
							//server.moveEntityUntilItHitsSomething(tree, new Vector3f(0, -1, 0));
							placeGenericModel(tree, (x*NEW_SECTOR_SIZE) + NumberFunctions.rndFloat(-NEW_SECTOR_SIZE/3, NEW_SECTOR_SIZE/3), 2f, (z*NEW_SECTOR_SIZE) + NumberFunctions.rndFloat(-NEW_SECTOR_SIZE/3, NEW_SECTOR_SIZE/3));
							float treeHeight = JMEModelFunctions.getLowestHeightAtPoint(tree.getMainNode(), server.getGameNode());
							tree.getMainNode().getLocalTranslation().y = treeHeight;
						} else if (map[x][z] == CLIFF) {
							Floor floor = new Floor(server, server.getNextEntityID(), "Cliff", (x*NEW_SECTOR_SIZE), height, (z*NEW_SECTOR_SIZE), NEW_SECTOR_SIZE, height, NEW_SECTOR_SIZE, "Textures/mud.png");
							server.actuallyAddEntity(floor);
						}
					}
				}
			}

			// Border
			float mapWidth = map.length * NEW_SECTOR_SIZE;
			float mapDepth = map[0].length * NEW_SECTOR_SIZE;
			float thick = 1f;
			MapBorder borderL = new MapBorder(server, server.getNextEntityID(), -(NEW_SECTOR_SIZE/2) - (thick/2), 0, (mapDepth/2)-(NEW_SECTOR_SIZE/2), thick, mapDepth);
			server.actuallyAddEntity(borderL);
			MapBorder borderR = new MapBorder(server, server.getNextEntityID(), mapWidth-(NEW_SECTOR_SIZE/2) + (thick/2), 0,  (mapDepth/2)-(NEW_SECTOR_SIZE/2), thick, mapDepth);
			server.actuallyAddEntity(borderR);
			MapBorder borderBack = new MapBorder(server, server.getNextEntityID(), (mapWidth/2)-(NEW_SECTOR_SIZE/2), 0, mapDepth-(NEW_SECTOR_SIZE/2) + (thick/2), mapWidth, thick);
			server.actuallyAddEntity(borderBack);
			MapBorder borderFront = new MapBorder(server, server.getNextEntityID(), (mapWidth/2)-(NEW_SECTOR_SIZE/2), 0, -(NEW_SECTOR_SIZE/2) - (thick/2), mapWidth, thick);
			server.actuallyAddEntity(borderFront);

			//if (Globals.DEBUG_PLAYER_START_POS) {
			/*DebuggingSphere ds = new DebuggingSphere(server,server.getNextEntityID(), 1f, 4f, 1f, true, false);
			server.actuallyAddEntity(ds);
			ds = new DebuggingSphere(server,server.getNextEntityID(), 3f, 5f, 1f, true, false);
			server.actuallyAddEntity(ds);
			ds = new DebuggingSphere(server,server.getNextEntityID(), 6f, 6f, 1f, true, false);
			server.actuallyAddEntity(ds);*/
			//}

			// Units
			{
				if (!Globals.NO_AI_UNITS) {
					// Place AI
					int numAI = NUM_AI_SOLDIERS;
					if (NUM_AI_SOLDIERS < 0) {
						numAI = (int)(mapWidth * mapDepth * AI_SOLDIERS_PER_SECTOR);
					}
					Globals.p("Creating " + numAI + " AI");
					for (int num=0 ; num<numAI ; num++) {
						Vector3f pos = getStartPos();
						TWIP_AISoldier s = new TWIP_AISoldier(server, server.getNextEntityID(), pos.x, pos.y + 5, pos.z, AbstractAvatar.ANIM_IDLE, "Enemy " + (num+1));
						server.actuallyAddEntity(s);
					}
				}
			}

			Globals.p("Finished loading map");

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	private GenericStaticModel getModel(int code) {
		float scale = NEW_SECTOR_SIZE / ORIGINAL_SECTOR_SIZE; 

		switch (code) {
		case 0:
			return null;

		case GRASS:
		case HOUSE:
		case CLIFF:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass", "Models/landscape_asset_v2a/obj/grass.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(), false, scale);

		case WATER:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Water", "Models/landscape_asset_v2a/obj/water.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(), false, scale);

		case STRAIGHT_ROAD_LR_LOW:
			GenericStaticModel roadLR = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-straight-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(), false, scale);
			return roadLR;

		case STRAIGHT_ROAD_UD_LOW:
			GenericStaticModel roadUD = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadUD", "Models/landscape_asset_v2a/obj/road-straight-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);
			return roadUD;

		case BEACH_TOP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"beachTop", "Models/landscape_asset_v2a/obj/water-beach-straight.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);

		case BEACH_BOTTOM:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"beachBottom", "Models/landscape_asset_v2a/obj/water-beach-straight.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case ROAD_BEND_RD_LOW:
			GenericStaticModel road2 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadRD", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);
			return road2;

		case ROAD_BEND_LD_LOW:
			GenericStaticModel road3 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLD", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);
			return road3;

		case ROAD_BEND_LU_LOW:
			GenericStaticModel road4 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLU", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);
			return road4;

		case ROAD_BEND_RU_LOW:
			GenericStaticModel road5 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadRU", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);
			return road5;

		case ROAD_TJUNC_D_LOW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-tjunction-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case ROAD_TJUNC_U_LOW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-tjunction-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);

		case ROAD_TJUNC_L_LOW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-tjunction-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);

		case ROAD_TJUNC_R_LOW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-tjunction-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);

		case ROAD_CROSSROADS_LOW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-crossing-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case HILL_RAMP_E_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp E", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case HILL_RAMP_W_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp W", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);

		case HILL_RAMP_S_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp N", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);

		case HILL_RAMP_N_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp S", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);

		case ROAD_HILL_E_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp E", "Models/landscape_asset_v2a/obj/road-hill.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case ROAD_HILL_W_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp W", "Models/landscape_asset_v2a/obj/road-hill.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);

		case ROAD_HILL_N_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp N", "Models/landscape_asset_v2a/obj/road-hill.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);

		case ROAD_HILL_S_UP:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp S", "Models/landscape_asset_v2a/obj/road-hill.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);

			//-----------------------------------
		case CORNER_HILL_UP_NE:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp E", "Models/landscape_asset_v2a/obj/hill-corner-high.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case CORNER_HILL_UP_NW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp W", "Models/landscape_asset_v2a/obj/hill-corner-high.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);

		case CORNER_HILL_UP_SE:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp N", "Models/landscape_asset_v2a/obj/hill-corner-high.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);

		case CORNER_HILL_UP_SW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp S", "Models/landscape_asset_v2a/obj/hill-corner-high.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);

			//-----------------------------------
		case CORNER_HILL_LOW_DOWN_NW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp E", "Models/landscape_asset_v2a/obj/hill-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case CORNER_HILL_LOW_DOWN_NE:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp W", "Models/landscape_asset_v2a/obj/hill-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);

		case CORNER_HILL_LOW_DOWN_SW:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp N", "Models/landscape_asset_v2a/obj/hill-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);

		case CORNER_HILL_LOW_DOWN_SE:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp S", "Models/landscape_asset_v2a/obj/hill-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);


		default:
			throw new RuntimeException("Invalid map code: " + code);
		}
	}


	private void placeGenericModel(GenericStaticModel model, float x, float y, float z) {
		//Vector3f pos = new Vector3f(x, y, z);
		model.setWorldTranslation(x, y, z);
		server.actuallyAddEntity(model); // model.getMainNode().getWorldBound()
	}


}
