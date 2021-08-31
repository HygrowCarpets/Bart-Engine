package com.Bart.Engine.gfx;

public class ImageTile extends Image {
	
	private int tileW, tileH;

	public ImageTile(String path, int tileW, int tileH) {
		super(path);
		this.tileW = tileW;
		this.tileH = tileH;
	}
	
	public ImageTile(String path, int tileW, int tileH, boolean alpha) {
		super(path, alpha);
		this.tileW = tileW;
		this.tileH = tileH;
	}
	
	public Image getTileImage(int posX, int posY) {
		Image result;
		
		int p[] = new int[tileW * tileH];
		
		for(int y = 0; y < tileW; y++) {
			for(int x = 0; x < tileH; x++) {
				p[x + y * tileW] = this.getP()[ ( x + posX * tileH) + ( y + posY * tileH ) * this.getWidth()];
			}
		}
		
		return new Image(p, tileW, tileH);
	}

	public int getTileWidth() {
		return tileW;
	}

	public void setTileWidth(int tileWidth) {
		this.tileW = tileWidth;
	}

	public int getTileHeight() {
		return tileH;
	}

	public void setTileHeight(int tileHeight) {
		this.tileH = tileHeight;
	}
}
