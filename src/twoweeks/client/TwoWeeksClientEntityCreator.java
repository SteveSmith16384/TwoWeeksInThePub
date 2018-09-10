package twoweeks.client;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.ExplosionShard;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

import twoweeks.abilities.PlayersMachineGun;
import twoweeks.entities.CarEnemyAvatar;
import twoweeks.entities.Floor;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MapBorder;
import twoweeks.entities.MercEnemyAvatar;
import twoweeks.entities.PlayerCarClientAvatar;
import twoweeks.entities.PlayerMercClientAvatar;
import twoweeks.entities.Bullet;
import twoweeks.entities.TWIP_AISoldier;
import twoweeks.entities.Terrain1;

public class TwoWeeksClientEntityCreator {

	public static final int SOLDIER_AVATAR = 1;
	public static final int TERRAIN1 = 2;
	public static final int FLOOR = 3;
	public static final int GENERIC_STATIC_MODEL = 4;
	public static final int CRATE = 5;
	public static final int BULLET = 7;
	public static final int MACHINE_GUN = 8;
	public static final int AI_SOLDIER = 10;
	public static final int MAP_BORDER = 11;
	public static final int CAR_AVATAR = 18;


	public TwoWeeksClientEntityCreator() {
	}


	public static String TypeToString(int type) {
		switch (type) {
		case SOLDIER_AVATAR: return "Avatar";
		case FLOOR: return "FLOOR";
		case CRATE: return "CRATE";
		case BULLET: return "PLAYER_LASER_BULLET";
		case MACHINE_GUN: return "LASER_RIFLE";
		case MAP_BORDER: return "INVISIBLE_MAP_BORDER";
		default: return "Unknown (" + type + ")";
		}
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityData msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + TypeToString(msg.type));
		}*/
		int id = msg.entityID;
		Vector3f pos = (Vector3f)msg.data.get("pos");

		switch (msg.type) {
		case SOLDIER_AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			byte side = (byte)msg.data.get("side");
			//float moveSpeed = (float)msg.data.get("moveSpeed");
			//float jumpForce = (float)msg.data.get("jumpForce");
			String playersName = (String)msg.data.get("playersName");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new PlayerMercClientAvatar(game, id, game.input, game.getCamera(), id, pos.x, pos.y, pos.z, side);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractOtherPlayersAvatar avatar = new MercEnemyAvatar(game, SOLDIER_AVATAR, id, pos.x, pos.y, pos.z, side, playersName);
				return avatar;
			}
		}

		case CAR_AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			byte side = (byte)msg.data.get("side");
			//float moveSpeed = (float)msg.data.get("moveSpeed");
			String playersName = (String)msg.data.get("playersName");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new PlayerCarClientAvatar(game, id, game.input, game.getCamera(), id, pos.x, pos.y, pos.z, side);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractOtherPlayersAvatar avatar = new CarEnemyAvatar(game, CAR_AVATAR, id, pos.x, pos.y, pos.z, side, playersName);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String name = (String)msg.data.get("name");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, name, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case GENERIC_STATIC_MODEL:
		{
			String name = (String)msg.data.get("name");
			String modelFile = (String)msg.data.get("modelFile");
			String tex = (String)msg.data.get("tex");
			float height = (float)msg.data.get("height");
			Vector3f dir = (Vector3f)msg.data.get("dir");
			boolean moveToFloor = (boolean)msg.data.get("moveToFloor");
			float scale = (float)msg.data.get("scale");
			GenericStaticModel generic = new GenericStaticModel(game, id, TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, name, modelFile, height, tex, pos.x, pos.y, pos.z, dir, moveToFloor, scale);
			return generic; // generic.getMainNode().getChild(0).getLocalRotation();
		}

		case TERRAIN1:
		{
			Terrain1 floor = new Terrain1(game, id, pos.x, pos.y, pos.z, null);
			return floor;
		}

		case MACHINE_GUN:
		{
			int ownerid = (int)msg.data.get("ownerid");
			int playerID = (int) msg.data.get("playerID");
			//if (game.currentAvatar != null && ownerid == game.currentAvatar.id) { // Don't care about other's abilities
			//if (playerID == game.playerID) { // Don't care about other's abilities
				//AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
			byte num = (byte)msg.data.get("num");
				PlayersMachineGun gl = new PlayersMachineGun(game, id, playerID, null, ownerid, num, null);
				return gl;
			//}
			//return null;

		}

		case BULLET:
		{
			byte side = (byte) msg.data.get("side");
			int playerID = (int) msg.data.get("playerID");
			int shooterId =  (int) msg.data.get("shooterID");
			IEntity shooter = game.entities.get(shooterId);
			Vector3f startPos = (Vector3f) msg.data.get("startPos");
			Vector3f dir = (Vector3f) msg.data.get("dir");
			Bullet bullet = new Bullet(game, game.getNextEntityID(), playerID, shooter, startPos, dir, side, null); // Notice we generate our own id
			return bullet;
		}

		case AI_SOLDIER:
		{
			byte side = (byte)msg.data.get("side");
			int animcode = (int)msg.data.get("animcode");
			String name = (String)msg.data.get("name");
			TWIP_AISoldier soldier = new TWIP_AISoldier(game, id, pos.x, pos.y, pos.z, side, animcode, name);
			return soldier;
		}

		case MAP_BORDER:
		{
			float w = (float)msg.data.get("w");
			float d = (float)msg.data.get("d");
			MapBorder hill = new MapBorder(game, id, pos.x, pos.y, pos.z, w, d);
			return hill;
		}
/*
		case CRATE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			SpaceCrate crate = new SpaceCrate(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;
		}
		 */
		case Globals.DEBUGGING_SPHERE:
		{
			DebuggingSphere sphere = new DebuggingSphere(game, id, pos.x, pos.y, pos.z, true, false);
			return sphere;
		}

		case Globals.EXPLOSION_SHARD:
			Vector3f forceDirection = (Vector3f) msg.data.get("forceDirection");
			float size = (float) msg.data.get("size");
			String tex = (String) msg.data.get("tex");
			ExplosionShard expl = new ExplosionShard(game, pos.x, pos.y, pos.z, size, forceDirection, tex);
			return expl;
			
		default:
			throw new RuntimeException("Unknown entity type for creation: " + msg.type);
		}
	}

}