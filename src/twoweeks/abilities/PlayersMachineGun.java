package twoweeks.abilities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.Bullet;

public class PlayersMachineGun extends AbstractMagazineGun implements IAbility {

	private static final int MAG_SIZE = 10;
	
	public PlayersMachineGun(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, byte abilityNum, ClientData client) {
		super(game, id, TwoWeeksClientEntityCreator.MACHINE_GUN, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, MAG_SIZE, client);

	}


	@Override
	protected Bullet createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte side) {
		return new Bullet(game, entityid, playerID, _shooter, startPos, _dir, side, client);
	}
	
}
