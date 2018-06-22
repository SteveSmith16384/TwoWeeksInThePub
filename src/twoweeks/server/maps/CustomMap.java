package twoweeks.server.maps;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MapBorder;
import twoweeks.server.TwoWeeksServer;

public class CustomMap implements IMapCreator {
	/*
	private static int map[][] = new int[][] {
		{1, 2} 
	};
	 */
	/*
	private static int map[][] = new int[][] {
		{ 4, 4, 4, 4, 4, 4, 4, 4, 4, 4}, 
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 
		{ 1, 5, 2, 2,10, 2, 2, 2, 6, 1}, 
		{ 1, 3, 9, 9, 3, 1, 9, 9, 3, 1}, 
		{ 1, 3, 1, 9, 3, 1, 9, 1, 3, 1}, 
		{ 1,13, 9, 9,16, 2, 2, 2,12, 1}, 
		{ 1, 3, 9, 1, 3, 1, 9, 9, 3, 1}, 
		{ 1, 8, 2, 2,11, 2, 2, 2, 7, 1}, 
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 
		{15,15,15,15,15,15,15,15,15,15}, 
		{14,14,14,14,14,14,14,14,14,14} 
	};
	 */
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
	private static final int HILL_RAMP_E = 17;
	private static final int HILL_RAMP_W = 18;
	private static final int HILL_RAMP_N = 19;
	private static final int HILL_RAMP_S = 20;

	private static final float ACTUAL_SECTOR_SIZE = 8;
	private static final float SECTOR_SIZE = 5;

	private TwoWeeksServer server;

	private int[][] map;/* = new int[][] {
		{5, 2, 6} 
	};*/

	public CustomMap(TwoWeeksServer _server) {
		server = _server;	
	}



	@Override
	public Vector3f getStartPos() {
		if (Globals.PLAYERS_START_IN_CORNER) {
			float x = NumberFunctions.rndFloat(1, 8);
			float z = NumberFunctions.rndFloat(1, 8);
			return new Vector3f(x, 20f, z);
			//return new Vector3f(map.length/2, 30f, map[0].length/2);
		} else {
			float x = NumberFunctions.rndFloat(1, (map.length * SECTOR_SIZE)-1);
			float z = NumberFunctions.rndFloat(1, (map.length * SECTOR_SIZE)-1);
			return new Vector3f(x, 20f, z);
		}
	}


	@Override
	public void createMap() {		
		try {
			Globals.p("Loading map from file...");
			//String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/test_map.csv").toURI())));
			String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/large_map1.csv").toURI())));
			String[] lines = text.split(System.lineSeparator());

			map = new int[lines[0].split(",").length][lines.length];

			for (int lineNum=0 ; lineNum<lines.length ; lineNum++) {
				String line = lines[lineNum];
				String[] tokens = line.split(",");
				for (int x=0 ; x<tokens.length ; x++) {
					map[x][lineNum] = Integer.parseInt(tokens[x].trim());
				}
			}

			for (int z=0 ; z<map[0].length ; z++) {
				for (int x=0 ; x<map.length ; x++) {
					GenericStaticModel model = getModel(map[x][z]);
					if (model != null) {
						placeGenericModel(model, x*SECTOR_SIZE, 0f, z*SECTOR_SIZE);
					}

					if (map[x][z] == HOUSE) {
						GenericStaticModel house = server.getRandomBuilding(new Vector3f(0, 0, 0));
						placeGenericModel(house, x*SECTOR_SIZE, 2f, z*SECTOR_SIZE);
						server.moveEntityUntilItHitsSomething(house, new Vector3f(0, -1, 0));
					} else if (map[x][z] == STRAIGHT_ROAD_UD_LOW) {
						GenericStaticModel car = server.getRandomVehicle(new Vector3f(0, 0, 0));
						placeGenericModel(car, x*SECTOR_SIZE, 2f, z*SECTOR_SIZE);
						server.moveEntityUntilItHitsSomething(car, new Vector3f(0, -1, 0));
					} else if (map[x][z] == GRASS) {
						GenericStaticModel tree = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/BigTreeWithLeaves.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", x, 0, z, new Vector3f(), true, 1f);
						placeGenericModel(tree, (x*SECTOR_SIZE) + NumberFunctions.rndFloat(-SECTOR_SIZE/2, SECTOR_SIZE/2), 2f, (z*SECTOR_SIZE) + NumberFunctions.rndFloat(-SECTOR_SIZE/2, SECTOR_SIZE/2));
						server.moveEntityUntilItHitsSomething(tree, new Vector3f(0, -1, 0));
					}
				}
			}
			
			float mapsize = map.length * SECTOR_SIZE;
			// Border
			MapBorder borderL = new MapBorder(server, server.getNextEntityID(), 0, 0, 0, mapsize, Vector3f.UNIT_Z);
			server.actuallyAddEntity(borderL);
			MapBorder borderR = new MapBorder(server, server.getNextEntityID(), mapsize+MapBorder.BORDER_WIDTH, 0, 0, mapsize, Vector3f.UNIT_Z);
			server.actuallyAddEntity(borderR);
			MapBorder borderBack = new MapBorder(server, server.getNextEntityID(), 0, 0, mapsize, mapsize, Vector3f.UNIT_X);
			server.actuallyAddEntity(borderBack);
			MapBorder borderFront = new MapBorder(server, server.getNextEntityID(), 0, 0, -MapBorder.BORDER_WIDTH, mapsize, Vector3f.UNIT_X);
			server.actuallyAddEntity(borderFront);

			Globals.p("Finished loading map");

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	private GenericStaticModel getModel(int code) {
		float scale = SECTOR_SIZE / ACTUAL_SECTOR_SIZE; 

		switch (code) {
		case 0:
			return null;

		case GRASS:
		case HOUSE:
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

		case HILL_RAMP_E:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp E", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false, scale);

		case HILL_RAMP_W:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp W", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false, scale);

		case HILL_RAMP_N:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp N", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false, scale);

		case HILL_RAMP_S:
			return new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass Ramp S", "Models/landscape_asset_v2a/obj/hill-ramp.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false, scale);

		default: 
			throw new RuntimeException("Invalid map code: " + code);
		}
	}


	private void placeGenericModel(GenericStaticModel model, float x, float y, float z) {
		Vector3f pos = new Vector3f(x, y, z);
		model.setWorldTranslation(pos);
		server.actuallyAddEntity(model);
	}


}
