package twoweeks.server.maps;

import com.jme3.math.Vector3f;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.Terrain1;
import twoweeks.server.ITerrainHeightAdjuster;
import twoweeks.server.TwoWeeksServer;

public class TerrainMap implements IMapCreator, ITerrainHeightAdjuster {

	// Map data
	private static final int MAP_SIZE = 200; // Size to use
	private static final int CITY_X = 20;
	private static final int CITY_Z = 20;
	private static final int CITY_SIZE = 60;
	private static final float SPACE_BETWEEN_BUILDINGS = 6f;
	

	private TwoWeeksServer server;
	
	public TerrainMap(TwoWeeksServer _server) {
		server = _server;	
	}
	
	
	@Override
	public void createMap() {
		Terrain1 terrain = new Terrain1(server, server.getNextEntityID(), 0, 0, 0, server);
		server.actuallyAddEntity(terrain); // terrain.getMainNode().getWorldBound();
		// 1280 x 1280

		placeCity(CITY_X, CITY_Z);
		//placeCity(CITY_X+(CITY_SIZE*2), CITY_Z+(CITY_SIZE*2));

		{
			// Place trees
			for (int z=80; z<=120 ; z+= 10) {
				for (int x=80; x<=120 ; x+= 10) {
					GenericStaticModel tree6 = new GenericStaticModel(server, server.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/BigTreeWithLeaves.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", x, 0, z, new Vector3f(), true, 1f);
					placeGenericModel(tree6, x, z);
				}
			}
			for (int num=0 ; num<10 ; num++) {

			}
		}		
	}


	private void placeGenericModel(GenericStaticModel tree5, float x, float z) {
		Vector3f pos = new Vector3f(x, 0, z);
		pos.y = JMEModelFunctions.getLowestHeightAtPoint(tree5.getMainNode(), server.getGameNode());
		tree5.setWorldTranslation(pos);
		server.actuallyAddEntity(tree5); //tree5.getMainNode().getWorldBound();

	}


	private void placeCity(int sx, int sz) {
		// Add buildings
		for (int z=sz ; z<sz+CITY_SIZE ; z+=SPACE_BETWEEN_BUILDINGS) {
			for (int x=sx ; x<sx+CITY_SIZE ; x+=SPACE_BETWEEN_BUILDINGS) {
				if (NumberFunctions.rnd(1, 3) == 1) {
					Vector3f pos = new Vector3f(x, 0f, z);
					GenericStaticModel building = server.getRandomBuilding(pos);
					pos.y = JMEModelFunctions.getLowestHeightAtPoint(building.getMainNode(), server.getGameNode());
					building.setWorldTranslation(pos);
					server.actuallyAddEntity(building); //building.getMainNode().getWorldBound();
					//Globals.p("Placed building at " + pos);
				}
			}
		}

		// Add cars
		for (int z=sz+3 ; z<sz+CITY_SIZE ; z+=SPACE_BETWEEN_BUILDINGS) {
			for (int x=sx+3 ; x<sx+CITY_SIZE ; x+=SPACE_BETWEEN_BUILDINGS) {
				if (NumberFunctions.rnd(1, 3) == 1) {
					Vector3f pos = new Vector3f(x, 0f, z);
					GenericStaticModel vehicle = server.getRandomVehicle(pos);
					pos.y = JMEModelFunctions.getLowestHeightAtPoint(vehicle.getMainNode(), server.getGameNode());
					vehicle.setWorldTranslation(pos);
					server.actuallyAddEntity(vehicle); //building.getMainNode().getWorldBound();
					Globals.p("Placed vehicle at " + pos);
				}
			}
		}


	}


	@Override
	public Vector3f getStartPos() {
		float x = CITY_X + NumberFunctions.rndFloat(0, MAP_SIZE-20);
		float z = CITY_Z + NumberFunctions.rndFloat(0, MAP_SIZE-20);

		return JMEModelFunctions.getHeightAtPoint(x, z, server.getGameNode());
	}

/*
	public Vector3f getHeightAtPoint(float x, float z) {
		Ray r = new Ray(new Vector3f(x, 255, z), DOWN_VEC);
		CollisionResults crs = new CollisionResults();
		server.getGameNode().collideWith(r, crs);
		Vector3f pos = crs.getClosestCollision().getContactPoint();
		return pos;
	}

/*
	public float getLowestHeightAtPoint(Spatial s) {
		CollisionResults crs = new CollisionResults();
		BoundingBox bb = (BoundingBox)s.getWorldBound();

		float res = 9999f;

		Ray r1 = new Ray(new Vector3f(bb.getCenter().x-bb.getXExtent(), 255, bb.getCenter().z-bb.getZExtent()), DOWN_VEC);
		crs.clear();
		server.getGameNode().collideWith(r1, crs);
		Vector3f pos1 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos1.y);

		Ray r2 = new Ray(new Vector3f(bb.getCenter().x+bb.getXExtent(), 255, bb.getCenter().z-bb.getZExtent()), DOWN_VEC);
		crs.clear();
		server.getGameNode().collideWith(r2, crs);
		Vector3f pos2 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos2.y);

		Ray r3 = new Ray(new Vector3f(bb.getCenter().x-bb.getXExtent(), 255, bb.getCenter().z+bb.getZExtent()), DOWN_VEC);
		crs.clear();
		server.getGameNode().collideWith(r3, crs);
		Vector3f pos3 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos3.y);

		Ray r4 = new Ray(new Vector3f(bb.getCenter().x+bb.getXExtent(), 255, bb.getCenter().z+bb.getZExtent()), DOWN_VEC);
		crs.clear();
		server.getGameNode().collideWith(r4, crs);
		Vector3f pos4 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos4.y);

		return res;
	}
*/

	private void dropDebugSphere(Terrain1 terrain, float x, float z) {
		Vector3f pos = JMEModelFunctions.getHeightAtPoint(x, z, server.getGameNode());// crs.getClosestCollision().getContactPoint();
		DebuggingSphere ds = new DebuggingSphere(server, server.getNextEntityID(), pos.x, pos.y, pos.z, true, false);
		server.actuallyAddEntity(ds);
	}


	@Override
	public void adjustHeight(AbstractHeightMap heightmap) {
		float h = heightmap.getInterpolatedHeight(CITY_X, CITY_Z); 
		for (int z=CITY_Z ; z<CITY_Z+CITY_SIZE ; z++) {
			for (int x=CITY_X ; x<CITY_X+CITY_SIZE ; x++) {
				//Globals.p("x=" + x + ", z=" + z);
				heightmap.setHeightAtPoint(h, x, z);
			}			
		}

	}
}
