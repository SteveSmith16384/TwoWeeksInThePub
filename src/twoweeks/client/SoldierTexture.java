package twoweeks.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.scs.stevetech1.jme.PaintableImage;

import ssmith.lang.NumberFunctions;

/**
 * Rows:
 * 0 - Skin tone
 * 1 - Eyes and eyebrows
 * 2 - Hair
 * 3 - Shirt
 * 4 - Trousers
 *
 */
public class SoldierTexture {
	
	private static final int SIZE = 32;

	public SoldierTexture() {
		super();
	}
	
	
	public static Texture getTexture() {
		PaintableImage pi = new PaintableImage(SIZE, SIZE) {
			
			@Override
			public void paint(Graphics2D g) {
				for (int row=0 ; row<5 ; row++) {
					switch (row) {
					case 0: // Trousers
						g.setColor(getRandomTrousersColour());
						break;
					case 1: // Shirt
						g.setColor(getRandomShirtColour());
						break;
					case 2: // Hair
						g.setColor(getRandomHairColour());
						break;
					case 3: // Eyes and brows
						g.setColor(Color.black);
						break;
					case 4: // Skin
						g.setColor(getRandomSkinColour());
						break;
					}
					
					int sy = getRowStart(row);
					int ey = getRowStart(row+1)-1;
					g.fillRect(0, sy, SIZE, ey);
				}
			}
			
		};
		
		pi.refreshImage();
		return new Texture2D(pi);
	}
	
	
	private static int getRowStart(int row) {
		switch (row) {
		case 0:
			return 0;
		case 1:
			return 10;
		case 2:
			return 15;
		case 3:
			return 21;
		case 4:
			return 26;
		case 5:
			return 31;
			default:
				throw new RuntimeException("Invalid row:" + row);
		}
	}
		
	
	private static Color getRandomHairColour() {
		return Color.darkGray;
	}
	

	private static Color getRandomSkinColour() {
		return new Color(255,224,189);
	}
	

	private static Color getRandomShirtColour() {
		int i = NumberFunctions.rnd(1, 8);
		switch (i) {
		case 1: return Color.WHITE;
		case 2: return Color.BLUE;
		case 3: return Color.CYAN;
		case 4: return Color.gray;
		case 5: return Color.green;
		case 6: return Color.lightGray;
		case 7: return Color.orange;
		case 8: return Color.yellow;
		default: return Color.WHITE;
		}
	}
	

	private static Color getRandomTrousersColour() {
		return getRandomShirtColour();
	}
	
}
