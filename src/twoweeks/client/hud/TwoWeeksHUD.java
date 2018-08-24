package twoweeks.client.hud;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.gui.TextArea;

import ssmith.util.RealtimeInterval;
import twoweeks.client.TwoWeeksClient;

public class TwoWeeksHUD extends Node {

	private static final float LINE_SPACING = 10;
	private static final int MAX_LINES = 5;

	private static ColorRGBA defaultColour = ColorRGBA.Green;

	private RealtimeInterval updateHudTextInterval = new RealtimeInterval(1000);
	private TextArea log_ta;
	private List<String> logLines = new ArrayList<String>();
	private float hud_width, hud_height;
	private Camera cam;
	private Geometry damage_box;
	private ColorRGBA dam_box_col = new ColorRGBA(1, 0, 0, 0.0f);
	private boolean process_damage_box;
	private AbstractGameClient game;
	private static BitmapFont font_small;
	
	private BitmapText abilityGun, abilityOther, healthText; // Update instantly 
	private String debugText, gameStatus, gameTime, pingText, numPlayers, unitsRemaining;
	private BitmapText textArea; 

	public TwoWeeksHUD(AbstractGameClient _game, Camera _cam) { 
		super("HUD");

		game = _game;
		hud_width = _cam.getWidth();
		hud_height = _cam.getHeight();
		cam = _cam;
		
		font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		super.setLocalTranslation(0, 0, 0);
/*
		if (!Globals.HIDE_BELLS_WHISTLES) {
			this.addTargetter();
		}
	*/	
		/*if (Globals.DEBUG_HUD) {
			for (int i=0; i<100 ; i+=10) {
				BitmapText deleteme = new BitmapText(font_small, false);
				deleteme.setColor(ColorRGBA.White);
				deleteme.setLocalTranslation(i, i, 0);
				this.attachChild(deleteme);
				deleteme.setText("x" + i);
			}
		}*/

		float xPos = hud_width - 150f;

		textArea = new BitmapText(font_small, false);
		textArea.setColor(defaultColour);
		textArea.setLocalTranslation(xPos, hud_height/2, 0);
		this.attachChild(textArea);
		textArea.setText("Waiting for data...");

		float yPos = hud_height - LINE_SPACING;

		yPos -= LINE_SPACING;
		abilityGun = new BitmapText(font_small, false);
		abilityGun.setColor(defaultColour);
		abilityGun.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(abilityGun);

		yPos -= LINE_SPACING;
		abilityOther = new BitmapText(font_small, false);
		abilityOther.setColor(defaultColour);
		abilityOther.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(abilityOther);

		yPos -= LINE_SPACING;
		healthText = new BitmapText(font_small, false);
		healthText.setColor(defaultColour);
		healthText.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(healthText);

		log_ta = new TextArea("log", font_small, 6, "");
		log_ta.setColor(defaultColour);
		//log_ta.setLocalTranslation(hud_width/2, hud_height/2, 0);
		log_ta.setLocalTranslation(20, hud_height-20, 0);
		this.attachChild(log_ta);

		// Damage box
		{
			Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", this.dam_box_col);
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			damage_box = new Geometry("damagebox", new Quad(hud_width, hud_height));
			damage_box.move(0, 0, 0);
			damage_box.setMaterial(mat);
			this.attachChild(damage_box);
		}

		/*if (Settings.DEBUG_HUD) {
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", new ColorRGBA(1, 1, 0, 0.5f));
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Geometry testBox = new Geometry("testBox", new Quad(w/2, h/2));
			testBox.move(10, 10, 0);
			testBox.setMaterial(mat);
			this.attachChild(testBox);

			/*Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			//mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Texture t = game.getAssetManager().loadTexture("Textures/text/hit.png");
			//t.setWrap(WrapMode.Repeat);
			mat.setTexture("DiffuseMap", t);
			Geometry geom = new Geometry("Billboard", new Quad(w, h));
			geom.setMaterial(mat);
			geom.move(0, 0, 0);
			//geom.setQueueBucket(Bucket.Transparent);
			//geom.setLocalTranslation(-w/2, -h/2, 0);
			this.attachChild(geom);*/
		/*
			Picture pic = new Picture("HUD Picture");
			pic.setImage(game.getAssetManager(), "Textures/text/hit.png", true);
			pic.setWidth(w);
			pic.setHeight(h);
			//pic.setPosition(settings.getWidth()/4, settings.getHeight()/4);
			this.attachChild(pic);
		}*/


		this.updateGeometricState();

		this.setModelBound(new BoundingBox());
		this.updateModelBound();

	}


	public void processByClient(AbstractGameClient client, float tpf) {
		if (updateHudTextInterval.hitInterval()) {
			if (client.gameData != null) {
				this.setGameStatus(SimpleGameData.getStatusDesc(client.gameData.getGameStatus()));
				this.setGameTime(client.gameData.getTime(client.serverTime));
				if (client.playersList != null) {
					this.setNumPlayers(client.playersList.size());
				}
			}
			this.setPing(client.pingRTT);
			this.updateTextArea((TwoWeeksClient)client);
		}

		if (client.currentAvatar != null) {
			this.setHealthText((int)client.currentAvatar.getHealth());
			// These must be after we might use them, so the hud is correct
			if (client.currentAvatar.ability[0] != null) {
				setAbilityGunText(client.currentAvatar.ability[0].getHudText());
			}
			if (client.currentAvatar.ability[1] != null) {
				setAbilityOtherText(client.currentAvatar.ability[1].getHudText());
			}
		}

		if (process_damage_box) {
			this.dam_box_col.a -= (tpf/2);
			if (dam_box_col.a <= 0) {
				dam_box_col.a = 0;
				process_damage_box = false;
			}
		}

	}


	public void appendToLog(String s) {
		this.logLines.add(s);
		while (this.logLines.size() > MAX_LINES) {
			this.logLines.remove(0);
		}
		StringBuilder str = new StringBuilder();
		for(String line : this.logLines) {
			str.append(line + "\n");
		}
		this.log_ta.setText(str.toString());
	}


	private void updateTextArea(TwoWeeksClient client) {
		StringBuilder str = new StringBuilder();
		str.append(this.gameStatus + "\n");
		str.append(this.gameTime + "\n");
		str.append(this.pingText + "\n");
		str.append(this.numPlayers + "\n");
		str.append(this.unitsRemaining + "\n");
		/*if (client.currentAvatar != null) {
			str.append("Pos: " + client.currentAvatar.getWorldTranslation() + "\n");
		}*/
		//str.append(this.debugText + "\n");
		this.textArea.setText(str.toString());
		
	}
	
	
	public void setDebugText(String s) {
		//this.debugText.setText(s);
		this.debugText = s;
		//this.updateTextArea();
	}


	private void setGameStatus(String s) {
		//this.gameStatus.setText("Game Status: " + s);
		this.gameStatus = s;
		//this.updateTextArea();

	}


	private void setGameTime(String s) {
		//this.gameTime.setText(s);
		this.gameTime = s;
		//this.updateTextArea();
	
	}


	public void setAbilityGunText(String s) {
		this.abilityGun.setText(s);
		//this.updateTextArea();
	}


	public void setAbilityOtherText(String s) {
		this.abilityOther.setText(s);
		//this.updateTextArea();

	}


	public void setHealthText(int s) {
		this.healthText.setText("Health: " + s);
		//this.updateTextArea();
	}


	private void setPing(long i) {
		this.pingText = "Ping: " + i;
	}


	public void setUnitsRemaining(int i) {
		this.unitsRemaining = "Units Left: " + i;
	}


	private void setNumPlayers(int i) {
		this.numPlayers = "Num Players: " + i;
	}


	public void showDamageBox() {
		process_damage_box = true;
		this.dam_box_col.a = .5f;
		this.dam_box_col.r = 1f;
		this.dam_box_col.g = 0f;
		this.dam_box_col.b = 0f;
	}


	public void showCollectBox() {
		process_damage_box = true;
		this.dam_box_col.a = .3f;
		this.dam_box_col.r = 0f;
		this.dam_box_col.g = 1f;
		this.dam_box_col.b = 1f;
	}


	private void addTargetter() {
		Picture targetting_reticule = new Picture("HUD Picture");
		targetting_reticule.setImage(game.getAssetManager(), "Textures/centre_crosshairs.png", true);
		float crosshairs_w = cam.getWidth()/10;
		targetting_reticule.setWidth(crosshairs_w);
		float crosshairs_h = cam.getHeight()/10;
		targetting_reticule.setHeight(crosshairs_h);
		this.setLocalTranslation((cam.getWidth() - crosshairs_w)/2, (cam.getHeight() - crosshairs_h)/2, 0);
		this.attachChild(targetting_reticule);
	}

/*
	@Override
	public Node getRootNode() {
		return this;
	}


	@Override
	public void showMessage(String s) {
		this.appendToLog(s);
	}
	

	@Override
	public void addChild(Node n) {
		this.attachChild(n);
	}
*/
}
