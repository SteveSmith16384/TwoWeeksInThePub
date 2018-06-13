package twoweeks.server.maps;

import com.jme3.math.Vector3f;

import ssmith.lang.NumberFunctions;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.GenericStaticModel;
import twoweeks.server.TwoWeeksServer;

public class CustomMap implements IMapCreator {
	/*
	private static int map[][] = new int[][] {
		{1, 2} 
	};
	*/

	private static int map[][] = new int[][] {
		{4, 4, 4, 4, 4, 4, 4, 4, 4, 4}, 
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 
		{1, 5, 2, 2, 2, 2, 2, 2, 6, 1}, 
		{1, 3, 9, 1, 1, 1, 1, 9, 3, 1}, 
		{1, 3, 1, 1, 1, 1, 1, 1, 3, 1}, 
		{1, 3, 1, 1, 1, 1, 1, 1, 3, 1}, 
		{1, 3, 9, 1, 1, 1, 1, 9, 3, 1}, 
		{1, 8, 2, 2, 2, 2, 2, 2, 7, 1}, 
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1} 
	};

	// Map codes
	private static final int GRASS = 1;
	private static final int STRAIGHT_ROAD_LR_LOW = 2;
	private static final int STRAIGHT_ROAD_UD_LOW = 3;
	private static final int BEACH_TOP = 4;
	private static final int BENT_ROAD_RD_LOW = 5;
	private static final int BENT_ROAD_LD_LOW = 6;
	private static final int BENT_ROAD_LU_LOW = 7;
	private static final int BENT_ROAD_RU_LOW = 8;
	private static final int HOUSE = 9;

	private static final int SECTOR_SIZE = 8;

	private TwoWeeksServer server;

	public CustomMap(TwoWeeksServer _server) {
		server = _server;	
	}



	@Override
	public Vector3f getStartPos() {
		return new Vector3f(map.length/2, 30f, map[0].length/2);
		/*float x = 1 + NumberFunctions.rndFloat(0, map.length-1); todo - re-add
		float z = 1 + NumberFunctions.rndFloat(0, map[0].length);
		return new Vector3f(x, 20f, z);*/
	}


	@Override
	public void createMap() {
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
				}
			}
		}
	}


	private GenericStaticModel getModel(int code) {
		switch (code) {
		case 0:
			return null;
			
		case GRASS:
		case HOUSE:
			GenericStaticModel grass = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"Grass", "Models/landscape_asset_v2a/obj/grass.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(), false);
			return grass;

		case STRAIGHT_ROAD_LR_LOW:
			GenericStaticModel roadLR = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLR", "Models/landscape_asset_v2a/obj/road-straight-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false);
			return roadLR;

		case STRAIGHT_ROAD_UD_LOW:
			GenericStaticModel roadUD = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadUD", "Models/landscape_asset_v2a/obj/road-straight-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(), false);
			return roadUD;

		case BEACH_TOP:
			GenericStaticModel beachTop = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"beachTop", "Models/landscape_asset_v2a/obj/water-beach-straight.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false);
			return beachTop;

		case BENT_ROAD_RD_LOW:
			GenericStaticModel road2 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadRD", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(1, 0, 0), false);
			return road2;

		case BENT_ROAD_LD_LOW:
			GenericStaticModel road3 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLD", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, -1), false);
			return road3;

		case BENT_ROAD_LU_LOW:
			GenericStaticModel road4 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadLU", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(-1, 0, 0), false);
			return road4;

		case BENT_ROAD_RU_LOW:
			GenericStaticModel road5 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, 
					"roadRU", "Models/landscape_asset_v2a/obj/road-corner-low.obj", -1, "Models/landscape_asset_v2a/obj/basetexture.jpg", 
					0, 0, 0, new Vector3f(0, 0, 1), false);
			return road5;

		default: 
			throw new RuntimeException("Invalid code: " + code);
		}
	}

	
	private void placeGenericModel(GenericStaticModel model, float x, float y, float z) {
		Vector3f pos = new Vector3f(x, y, z);
		model.setWorldTranslation(pos);
		server.actuallyAddEntity(model);
	}


}
