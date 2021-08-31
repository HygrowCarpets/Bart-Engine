package com.Bart.Engine.gfx;

public class Font {
	
	public static final Font DEFAULT = new Font("/Fonts/TestFont.png");
	
	private Image fontImage;
	private int positions[];
	private int widths[];

	public Font(String path) {
		fontImage = new Image(path);
		
		positions = new int[59];
		widths = new int[59];
		
		int unicode = 0;
		
		for(int i = 0; i < fontImage.getWidth(); i++) {
			if(fontImage.getP()[i] == 0xff0000ff) {
				positions[unicode] = i;
			}
			else if(fontImage.getP()[i] == 0xffffff00) {
				widths[unicode] = i - positions[unicode];
				unicode++;
			}
		}
	}

	public Image getFontImage() {
		return fontImage;
	}

	public void setFontImage(Image fontImage) {
		this.fontImage = fontImage;
	}

	public int[] getOffsets() {
		return positions;
	}

	public void setOffsets(int[] positions) {
		this.positions = positions;
	}

	public int[] getWidths() {
		return widths;
	}

	public void setWidths(int[] widths) {
		this.widths = widths;
	}
}
