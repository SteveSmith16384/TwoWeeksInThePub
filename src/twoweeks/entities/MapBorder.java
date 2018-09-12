package twoweeks.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;

public class MapBorder extends PhysicalEntity {

	private static final boolean INVISIBLE = true;

	public static final float BORDER_HEIGHT = 30f; // Since players start high

	public MapBorder(IEntityController _game, int id, float x, float y, float z, float w, float d) {
		super(_game, id, TwoWeeksClientEntityCreator.MAP_BORDER, "InvisibleMapBorder", false, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("w", w);
			creationData.put("d", d);
		}

		Box box1 = new Box(w/2, BORDER_HEIGHT/2, d/2);
		//float w = BORDER_WIDTH;
		float h = BORDER_HEIGHT;
		//float d = size;
		//box1.scaleTextureCoordinates(new Vector2f(BORDER_WIDTH, BORDER_HEIGHT));
		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
				0, h, w, h, w, 0, 0, 0, // front
				0, h, d, h, d, 0, 0, 0, // left
				w, 0, w, d, 0, d, 0, 0, // top
				w, 0, w, d, 0, d, 0, 0  // bottom
		}));
		Geometry geometry = new Geometry("MapBorderBox", box1);
		if (!_game.isServer()) {			
			if (INVISIBLE) {
				geometry.setCullHint(CullHint.Always);
			} else {
				TextureKey key3 = new TextureKey("Textures/fence.png");
				key3.setGenerateMips(true);
				Texture tex3 = game.getAssetManager().loadTexture(key3);
				tex3.setWrap(WrapMode.Repeat);

				Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
				geometry.setMaterial(floor_mat);
			}
		}

		geometry.setLocalTranslation(0, BORDER_HEIGHT/2, 0); // move up
		mainNode.attachChild(geometry);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(0);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
	}


}
