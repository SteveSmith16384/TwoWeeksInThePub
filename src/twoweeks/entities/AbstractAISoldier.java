package twoweeks.entities;

import java.util.HashMap;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.IArtificialIntelligence;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAISoldier extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision,
IRewindable, IAnimatedClientSide, IAnimatedServerSide, IDrawOnHUD, IProcessByClient, IGetRotation, ISetRotation, IKillable, ITargetable {

	public static final int BULLETS_IN_MAG = 8;
	public static final float SHOOT_INTERVAL = .3f;
	public static final float RELOAD_INTERVAL = 3f;

	public static final float START_HEALTH = 15f;
	public static final float WALKING_SPEED = .53f;
	public static final float RUNNING_SPEED = 1.3f;

	private IAvatarModel soldierModel; // Need this to animate the model
	private float health = START_HEALTH;
	public int side;
	protected IArtificialIntelligence ai;
	private int serverSideCurrentAnimCode;
	private long timeKilled;

	// Weapon
	private int bullets = BULLETS_IN_MAG;
	private float timeToNextShot = 0; 

	// HUD
	private BitmapText hudNode;
	private static BitmapFont font_small;

	public AbstractAISoldier(IEntityController _game, int id, int type, float x, float y, float z, int _side, 
			IAvatarModel _model, int _csInitialAnimCode, String name) {
		super(_game, id, type, "AISoldier", true, false, true);

		side = _side;
		soldierModel = _model; // Need it for dimensions for bb


		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("name", name);
		} else {
			this.soldierModel.createAndGetModel(_side);
			game.getGameNode().attachChild(this.soldierModel.getModel());
			this.setAnimCode_ClientSide(_csInitialAnimCode);
		}

		// Create box for collisions
		Box box = new Box(soldierModel.getSize().x/2, soldierModel.getSize().y/2, soldierModel.getSize().z/2);
		Geometry bbGeom = new Geometry("bbGeom_" + name, box);
		bbGeom.setLocalTranslation(0, soldierModel.getSize().y/2, 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		bbGeom.setUserData(Globals.ENTITY, this);

		this.mainNode.attachChild(bbGeom);
		mainNode.setUserData(Globals.ENTITY, this);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this); // was false
		simpleRigidBody.canWalkUpSteps = true;
		simpleRigidBody.setBounciness(0);

		font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		hudNode = new BitmapText(font_small);
		hudNode.setText(name);

	}


	@Override
	public HashMap<String, Object> getCreationData() {
		HashMap<String, Object> creationData = super.getCreationData();
		// Need this in case the soldier is dead, in which case they won't send any updates, meaning
		// they won't get sent an animation code.
		creationData.put("animcode", this.getCurrentAnimCode());
		return creationData;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (health > 0) {
			timeToNextShot -= tpf_secs;
			//if (server.getGameData().getGameStatus() == SimpleGameData.ST_STARTED) { No, move around in deploy stage
				ai.process(server, tpf_secs);
			//}
			this.serverSideCurrentAnimCode = ai.getAnimCode();
		} else {
			/*if (this.serverSideCurrentAnimCode != AbstractAvatar.ANIM_DIED) { // This should never be needed
				Globals.p("Warning: Manually setting death anim");
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_DIED;
				this.sendUpdate = true;
			}*/
			this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving

			if (Globals.REMOVE_DEAD_SOLDIERS) {
				long diff = System.currentTimeMillis() - timeKilled;
				if (diff > 10000) {
					this.remove();
					return;
				}
			}
		}

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		this.soldierModel.getModel().setLocalTranslation(this.getWorldTranslation()); // this.soldierModel.setAnim(anim);
	}


	@Override
	public void fallenOffEdge() {
		this.remove();
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		if (Globals.DEBUG_BULLET_HIT) {
			Globals.p(this + " damaged()");
		}
		if (health > 0) {
			this.health -= amt;
			ai.wounded(collider);
			if (health <= 0) {
				if (Globals.DEBUG_BULLET_HIT) {
					Globals.p(this + " killed");
				}
				AbstractGameServer server = (AbstractGameServer)game;
				server.gameNetworkServer.sendMessageToAll(new EntityKilledMessage(this, collider, reason));
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_DIED;
				this.sendUpdate = true; // Send new anim code

				this.game.getPhysicsController().removeSimpleRigidBody(this.simpleRigidBody); // Prevent us colliding
				this.simpleRigidBody.setMovedByForces(false);

				this.timeKilled = System.currentTimeMillis();
				
				this.collideable = false;
			}
		}
	}


	@Override
	public void remove() {
		super.remove();

		if (soldierModel.getModel() != null) {
			this.soldierModel.getModel().removeFromParent();
		}
	}


	@Override
	public int getSide() {
		return side;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (health > 0) {
			if (game.isServer()) {
				ai.collided(pe);
			}
		}
	}


	@Override
	public void setAnimCode_ClientSide(int animCode) {
		if (soldierModel != null) {
			this.soldierModel.setAnim(animCode);
		}
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// Do nothing, already handled
	}


	/**
	 * Called server-side only,
	 */
	@Override
	public int getCurrentAnimCode() {
		return this.serverSideCurrentAnimCode;
	}


	@Override
	public void drawOnHud(IHUD hud, Camera cam) {
		// No
	}


	@Override
	public Node getHUDItem() {
		return this.hudNode;
	}


	@Override
	public void setRotation(Vector3f dir) {
		Vector3f dir2 = new Vector3f(dir.x, 0, dir.z); 
		JMEAngleFunctions.rotateToWorldDirection(this.soldierModel.getModel(), dir2);
	}


	@Override
	public Vector3f getRotation() {
		return ai.getDirection();
	}


	@Override
	public void handleKilledOnClientSide(PhysicalEntity killer) {
		this.hudNode.removeFromParent();
	}


	@Override
	public boolean isValidTargetForSide(int shootersSide) {
		return shootersSide != this.side;
	}


	public void shoot(PhysicalEntity target) {
		//if (this.shootInt.hitInterval()) {
		if (this.timeToNextShot <= 0) {
			if (Globals.DEBUG_AI_BULLET_POS) {
				Globals.p("AI shooting!  AI at " + this.getWorldTranslation());
			}
			Vector3f pos = this.getWorldTranslation().clone(); // Must clone otherwsei AI jumps when shooting
			pos.y += this.soldierModel.getBulletStartHeight();
			Vector3f dir = target.getMainNode().getWorldBound().getCenter().subtract(pos).normalizeLocal();
			AbstractAIBullet bullet = this.createBullet(pos, dir);// new AIBullet(game, game.getNextEntityID(), side, pos.x, pos.y, pos.z, this, dir);
			this.game.addEntity(bullet);

			this.bullets--;
			if (this.bullets > 0) {
				this.timeToNextShot = SHOOT_INTERVAL;
			} else {
				this.timeToNextShot = RELOAD_INTERVAL;
				bullets = BULLETS_IN_MAG;
				//Globals.p("AI Reloading");
			}
		}
	}


	protected abstract AbstractAIBullet createBullet(Vector3f pos, Vector3f dir);


	@Override
	public boolean isAlive() {
		return this.health > 0;
	}


	@Override
	public float getHealth() {
		return health;
	}


	public PhysicalEntity getPhysicalEntity() {
		return this;
	}


	@Override
	public int getTargetPriority() {
		return 2;
	}


}
