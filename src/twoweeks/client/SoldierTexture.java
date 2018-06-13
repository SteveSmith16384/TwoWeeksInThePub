package twoweeks.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.scs.stevetech1.jme.PaintableImage;

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
					case 0: // Skin
						g.setColor(Color.green);
						break;
					case 1: // Eyes and brows
						g.setColor(Color.white);
						break;
					case 2: // Hair
						g.setColor(getRandomHairColour());//Color.black);
						break;
					case 3: // Shirt
						g.setColor(Color.gray);
						break;
					case 4: // Trousers
						g.setColor(Color.gray);
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
		return Color.black;
	}
	
}
