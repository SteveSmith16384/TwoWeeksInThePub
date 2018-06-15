package twoweeks.server.ai;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.IArtificialIntelligence;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;
import twoweeks.entities.AIBullet;
import twoweeks.entities.AbstractAISoldier;
import twoweeks.entities.Floor;
import twoweeks.entities.Terrain1;

public class TWIPSoldierAI3 implements IArtificialIntelligence {

	private static final float VIEW_ANGLE_RADS = 1f;
	private static final boolean SHOOT_AT_ENEMY = true; // todo

	private AbstractAISoldier soldierEntity;
	private Vector3f currDir;
	private RealtimeInterval checkForEnemyInt = new RealtimeInterval(1000);
	private ITargetable currentTarget;
	private int animCode = 0;
	private Vector3f prevPos = new Vector3f(); // To check if we've moved

	private float waitForSecs = 0; // e.g. wait for door to open

	public TWIPSoldierAI3(AbstractAISoldier _pe) {
		soldierEntity = _pe;

		currDir = new Vector3f();
		changeDirection(getRandomDirection()); // Start us pointing in the right direction
	}


	@Override
	public void process(AbstractGameServer server, float tpf_secs) {
		if (this.waitForSecs > 0) {
			this.waitForSecs -= tpf_secs;
		} 

		if (currentTarget != null) { // Find enemy
			boolean cansee = soldierEntity.canSee((PhysicalEntity)this.currentTarget, AIBullet.RANGE, VIEW_ANGLE_RADS);
			if (!cansee) {
				this.currentTarget = null;
				if (Globals.DEBUG_AI_TARGETTING) {
					Globals.p("AI no longer see target");
				}
			}
		}
		if (currentTarget == null) { // Check we can still see enemy
			if (this.checkForEnemyInt.hitInterval()) {
				currentTarget = server.getTarget(this.soldierEntity, this.soldierEntity.side, AIBullet.RANGE, VIEW_ANGLE_RADS);
				if (Globals.DEBUG_AI_TARGETTING) {
					Globals.p("AI can now see " + currentTarget);
				}
			}
		} else { // Face enemy
			PhysicalEntity pe = (PhysicalEntity)this.currentTarget;
			Vector3f dir = pe.getWorldTranslation().subtract(this.soldierEntity.getWorldTranslation()); // todo - don't create each time
			dir.y = 0;
			dir.normalizeLocal();
			this.changeDirection(dir);
		}

		if (currentTarget != null && SHOOT_AT_ENEMY) {
			soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
			animCode = AbstractAvatar.ANIM_IDLE;
			this.soldierEntity.shoot((PhysicalEntity)currentTarget);
		} else if (waitForSecs <= 0) {
			soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AbstractAISoldier.SPEED)); // Walk forwards
			animCode = AbstractAvatar.ANIM_WALKING;
		} else {
			soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
			animCode = AbstractAvatar.ANIM_IDLE;
		}

		// If we've failed to move when we should, change direction
		if (soldierEntity.simpleRigidBody.getAdditionalForce().length() > 0) {
			if (prevPos.distance(this.soldierEntity.getWorldTranslation()) == 0) {
				changeDirection(getRandomDirection());
			}
		}
		prevPos.set(this.soldierEntity.getWorldTranslation());
	}


	@Override
	public void collided(PhysicalEntity pe) {
		/*if (pe instanceof Floor == false) {
			// Change direction to away from blockage, unless it's a doior
			if (pe instanceof Terrain1 == false) {
				//Globals.p("AISoldier has collided with " + pe);
				//changeDirection(currDir.mult(-1));
				changeDirection(getRandomDirection());
			}
		}*/
	}


	private void changeDirection(Vector3f dir) {
		//Globals.p("Changing direction to " + dir);
		this.currDir.set(dir.normalizeLocal());
		soldierEntity.getMainNode().lookAt(soldierEntity.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
	}


	private static Vector3f getRandomDirection() {
		int i = NumberFunctions.rnd(0,  7);
		switch (i) {
		case 0: return new Vector3f(1f, 0, 0);
		case 1: return new Vector3f(-1f, 0, 0);
		case 2: return new Vector3f(0f, 0, 1f);
		case 3: return new Vector3f(0f, 0, -1f);

		// Diagonals
		case 4: return new Vector3f(1f, 0, 1f);
		case 5: return new Vector3f(-1f, 0, -1f);
		case 6: return new Vector3f(-1f, 0, 1f);
		case 7: return new Vector3f(1f, 0, -1f);
}
		throw new RuntimeException("Invalid direction: " + i);
	}


	@Override
	public Vector3f getDirection() {
		return currDir;
	}


	@Override
	public int getAnimCode() {
		return animCode;
	}


	@Override
	public ITargetable getCurrentTarget() {
		return this.currentTarget;
	}


	@Override
	public void wounded(ICausesHarmOnContact collider) {
		if (collider.getActualShooter() != null) {
			PhysicalEntity pe = (PhysicalEntity)collider.getActualShooter();
			Vector3f dir = pe.getWorldTranslation().subtract(soldierEntity.getWorldTranslation()).normalizeLocal();
			this.changeDirection(dir);
		}
	}

}
