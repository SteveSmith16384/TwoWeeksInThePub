package twoweeks.server.ai;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.data.SimpleGameData;
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
import twoweeks.entities.MapBorder;

public class TWIPSoldierAI3 implements IArtificialIntelligence {

	private static final float VIEW_ANGLE_RADS = 1f;
	//private static final boolean SHOOT_AT_ENEMY = true; // todo

	private AbstractAISoldier soldierEntity;
	private Vector3f currDir;
	private RealtimeInterval checkForEnemyInt = new RealtimeInterval(1000);
	private ITargetable currentTarget;
	private float distToTarget;
	private int animCode = 0;
	private Vector3f prevPos = new Vector3f(); // To check if we've moved
	private boolean runs = true;
	private float waitForSecs = 0; // e.g. wait for door to open
	private Vector3f tmpDir = new Vector3f();
	private Vector3f tmpMove = new Vector3f();

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
			boolean cansee = true;
			if (this.currentTarget.isAlive()) {
				cansee = soldierEntity.canSee((PhysicalEntity)this.currentTarget, AIBullet.RANGE, VIEW_ANGLE_RADS);
			} else {
				cansee = false;
			}
			if (!cansee) {
				this.currentTarget = null;
				runs = NumberFunctions.rnd(1,  2) == 1;
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
			Vector3f dir = pe.getWorldTranslation().subtract(this.soldierEntity.getWorldTranslation(), tmpDir);
			dir.y = 0;
			dir.normalizeLocal();
			this.changeDirection(dir);
			this.distToTarget = this.soldierEntity.distance((PhysicalEntity)this.currentTarget);
		}

		boolean shoots = server.getGameData().getGameStatus() == SimpleGameData.ST_STARTED;
		if (currentTarget != null && shoots) {
			//soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
			//animCode = AbstractAvatar.ANIM_IDLE;
			this.soldierEntity.shoot((PhysicalEntity)currentTarget);
			runs = false; // Walk towards target
			
			if (this.distToTarget < 3f) {
				soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
				animCode = AbstractAvatar.ANIM_IDLE;
				return; // Don't move!
			}
		}
		
		if (waitForSecs <= 0) {
			if (runs) {
				soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AbstractAISoldier.RUNNING_SPEED, tmpMove)); // Walk forwards
				animCode = AbstractAvatar.ANIM_RUNNING;
			} else {
				soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AbstractAISoldier.WALKING_SPEED, tmpMove)); // Walk forwards
				animCode = AbstractAvatar.ANIM_WALKING;
			}
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
		if (pe instanceof Floor == false) {
			// Change direction to away from blockage, unless it's a doior
			if (pe instanceof MapBorder) {
				//Globals.p("AISoldier has collided with " + pe);
				changeDirection(getRandomDirection());
			}
		}
	}


	private void changeDirection(Vector3f dir) {
		//Globals.p("Changing direction to " + dir);
		this.currDir.set(dir.normalizeLocal());
		soldierEntity.getMainNode().lookAt(soldierEntity.getWorldTranslation().add(currDir, tmpDir), Vector3f.UNIT_Y); // Point us in the right direction
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
			Vector3f dir = pe.getWorldTranslation().subtract(soldierEntity.getWorldTranslation(), tmpDir).normalizeLocal();
			this.changeDirection(dir);
			this.checkForEnemyInt.fireInterval();
		}
	}

}
