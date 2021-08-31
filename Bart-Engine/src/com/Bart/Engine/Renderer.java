package com.Bart.Engine;

import java.awt.image.*;
import java.util.ArrayList;
import java.util.Comparator;

import com.Bart.Engine.gfx.*;

public class Renderer {
	
	private int pW, pH;
	private int p[];
	
	private ArrayList<ImageRequest> imageRequests = new ArrayList<ImageRequest>();
	private ArrayList<LightRequest> lightRequests = new ArrayList<LightRequest>();
	
	private int zDepths[];
	private int currentZDepth = 0;
	
	private int lightMap[];
	private int lightBlock[];
	private int ambientColour = 0xff1c1717;
	
	private boolean processing = false;
	
	private Font font = Font.DEFAULT;
	
	public Renderer(GameContainer gc) {
		pW = gc.getWidth();
		pH = gc.getHeight();
		
		p = ((DataBufferInt)gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
		zDepths = new int[p.length];
		
		lightMap = new int[p.length];
		lightBlock = new int[p.length];
	}
	
	public void clear() {
		for(int i = 0; i < p.length; i++) {
			p[i] = 0x00000000;
			zDepths[i] = 0;
			lightMap[i] = ambientColour;
			lightBlock[i] =  0x00000000;
		}
	}
	
	public void process() {
		processing = true;
		imageRequests.sort(new Comparator<ImageRequest>() {
			@Override
			public int compare(ImageRequest ir1, ImageRequest ir2) {
				if(ir1.zDepth < ir2.zDepth) {
					return -1;
				}
				if(ir1.zDepth > ir2.zDepth) {
					return 1;
				}
				return 0;
			}
			
		});
		
		for(int i = 0; i < imageRequests.size(); i++) {
			setCurrentZDepth(imageRequests.get(i).zDepth);
			drawImage(imageRequests.get(i).image, imageRequests.get(i).posX, imageRequests.get(i).posY);
		}
		
		for(int i = 0; i < lightRequests.size(); i++) {
			LightRequest lightRequest = lightRequests.get(i);
			drawLightRequest(lightRequest.light, lightRequest.x, lightRequest.y);
		}
		
		for(int i = 0; i < p.length; i++) {
			float red = ( ( lightMap[i] >> 16 ) & 0xff ) / 255f;
			float green = ( ( lightMap[i] >> 8 ) & 0xff ) / 255f;
			float blue = ( lightMap[i] & 0xff ) / 255f;
			
			p[i] = (int) ( ( ( p[i] >> 16 ) & 0xff ) * red ) << 16| (int) ( ( ( p[i] >> 8 ) & 0xff ) * green ) << 8 | (int) ( ( p[i] & 0xff ) * blue );
		}
		
		processing = false;
		imageRequests.clear();
		lightRequests.clear();
	}
	
	public void setPixel(int x, int y, int colour) {
		int alpha = ((colour >> 24) & 0xff);
		if( (x < 0 || x >= pW || y < 0 || y >= pH) || alpha == 0) { return; }
		
		int index = x + y * pW;
		
		if(zDepths[index] > currentZDepth) { return; }
		
		zDepths[index] = currentZDepth;
		
		if(alpha == 255) {
			p[index] = colour;
		}
		else {
			int pixelColour = p[x + y * pW];
			
			int newRed = ( (pixelColour >> 16) & 0xff ) - (int) ( ( ( (pixelColour >> 16) & 0xff ) - ( ( (colour >> 16) & 0xff ) ) ) * (alpha / 255f) );
			int newGreen = ( (pixelColour >> 8) & 0xff ) - (int) ( ( ( (pixelColour >> 8) & 0xff ) - ( ( (colour >> 8) & 0xff ) ) ) * (alpha / 255f) );
			int newBlue = ( pixelColour & 0xff ) - (int) ( ( ( pixelColour & 0xff ) - ( colour & 0xff ) ) * (alpha / 255f) );
			
			p[index] = (newRed << 16 | newGreen << 8 | newBlue );
		}
	
	}

	public void setLightMap(int x, int y, int value) {
		if(x < 0 || x >= pW || y < 0 || y >= pH) {
			return; 
		}
		
		int baseColour = lightMap[x + y * pW];
		
		int maxRed = Math.max( (baseColour >> 16) & 0xff , (value >> 16) & 0xff  );
		int maxGreen = Math.max( (baseColour >> 8) & 0xff , (value >> 8) & 0xff );
		int maxBlue = Math.max( baseColour & 0xff , value & 0xff );
		
		lightMap[x + y * pW] = (maxRed << 16 | maxGreen << 8 | maxBlue );
	}
	
	public void setLightBlock(int x, int y, int value) {
		if(x < 0 || x >= pW || y < 0 || y >= pH) { return; }
		
		if(zDepths[x + y * pW] > currentZDepth) { return; }
		
		lightBlock[x + y * pW] = value;
	}
	
	public void drawImage(Image image, int posX, int posY) {
		if(image.isAlpha() && !processing) {
			imageRequests.add(new ImageRequest(image, currentZDepth, posX, posY));
			return;
		}
		
		//Code to not bother trying to render images off screen
		if(posX < -image.getWidth()) { return; } //off screen left
		if(posY < -image.getHeight()) { return; } //off screen top
		if(posX >= pW) { return; } //off screen right
		if(posY >= pH) { return; } //off screen bottom
		
		//Code to not bother trying to render parts of the image clipping off screen
		int newX = 0;
		int newY = 0;
		int newWidth = image.getWidth();
		int newHeight = image.getHeight();
		
		if(posX < 0) { newX -= posX; } //clipping left
		if(posY < 0) { newY -= posY; } //clipping top
		if(posX + newWidth >= pW) { newWidth -= (posX + newWidth) - pW; } //clipping right
		if(posY + newHeight >= pH) { newHeight -= (posY + newHeight) - pH; } //clipping bottom
		
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(x + posX, y + posY, image.getP()[x + y * image.getWidth()] );
				setLightBlock(x + posX, y + posY, image.getLightBlock());
			}
		}
	}
	
	public void drawImageTile(ImageTile image, int posX, int posY, int tileX, int tileY) {
		if(image.isAlpha() && !processing) {
			imageRequests.add(new ImageRequest(image.getTileImage(tileX, tileY), currentZDepth, posX, posY));
			return;
		}
		
		//Code to not bother trying to render images off screen
		if(posX < -image.getTileWidth()) { return; } //off screen left
		if(posY < -image.getTileHeight()) { return; } //off screen top
		if(posX >= pW) { return; } //off screen right
		if(posY >= pH) { return; } //off screen bottom
		
		//Code to not bother trying to render parts of the image clipping off screen
		int newX = 0;
		int newY = 0;
		int newWidth = image.getTileWidth();
		int newHeight = image.getTileHeight();
		
		if(posX < 0) { newX -= posX; } //clipping left
		if(posY < 0) { newY -= posY; } //clipping top
		if(posX + newWidth >= pW) { newWidth -= (posX + newWidth) - pW; } //clipping right
		if(posY + newHeight >= pH) { newHeight -= (posY + newHeight) - pH; } //clipping bottom
		
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(x + posX, y + posY, image.getP()[ ( x + tileX * image.getTileWidth() )  + ( y + tileY * image.getTileHeight() ) * image.getWidth()] );
				setLightBlock(x + posX, y + posY, image.getLightBlock());
			}
		}
	}
	
	public void drawText(String text, int posX, int posY, int colour) {
		text = text.toUpperCase();
		
		int offset = 0;
		
		for(int i = 0; i < text.length(); i++) {
			int unicode = text.codePointAt(i) - 32;
			
			for(int y = 0; y < font.getFontImage().getHeight(); y++) {
				for(int x = 0; x < font.getWidths()[unicode]; x++) {
					
					if(font.getFontImage().getP()[ (x + font.getOffsets()[unicode] ) + y * font.getFontImage().getWidth()] == 0xffffffff) {
						setPixel(x + posX + offset, y + posY, colour);
					}
				}
			}
			offset += font.getWidths()[unicode];
		}
	}

	public void drawRect(int posX, int posY, int width, int height, int colour) {
		//Code to not bother trying to render images off screen
		if(posX < -width) { return; } //off screen left
		if(posY < -height) { return; } //off screen top
		if(posX >= pW) { return; } //off screen right
		if(posY >= pH) { return; } //off screen bottom
		
		//Code to not bother trying to render parts of the image clipping off screen
		int newX = 0;
		int newY = 0;
		int newWidth = width;
		int newHeight = height;
		
		if(posX < 0) { newX -= posX; } //clipping left
		if(posY < 0) { newY -= posY; } //clipping top
		if(posX + newWidth >= pW) { newWidth -= (posX + newWidth) - pW; } //clipping right
		if(posY + newHeight >= pH) { newHeight -= (posY + newHeight) - pH; } //clipping bottom
		
		for(int y = newY; y <= newHeight; y++) {
			setPixel(posX, posY + y, colour);
			setPixel(posX + width, posY + y, colour);
		}
		
		for(int x = newX = 0; x <= newWidth; x++) {
			 setPixel(posX + x, posY, colour);
			 setPixel(posX + x, posY + height, colour);
		}
	}
	
	public void drawFillRect(int posX, int posY, int width, int height, int colour) {
		//Code to not bother trying to render images off screen
		if(posX < -width) { return; } //off screen left
		if(posY < -height) { return; } //off screen top
		if(posX >= pW) { return; } //off screen right
		if(posY >= pH) { return; } //off screen bottom
		
		//Code to not bother trying to render parts of the image clipping off screen
		int newX = 0;
		int newY = 0;
		int newWidth = width;
		int newHeight = height;
		
		if(posX < 0) { newX -= posX; } //clipping left
		if(posY < 0) { newY -= posY; } //clipping top
		if(posX + newWidth >= pW) { newWidth -= (posX + newWidth) - pW; } //clipping right
		if(posY + newHeight >= pH) { newHeight -= (posY + newHeight) - pH; } //clipping bottom
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(posX + x, posY + y, colour);
			}
		}
	}

	public void drawLight(Light light, int posX, int posY) {
		lightRequests.add(new LightRequest(light, posX, posY));
	}
	
	private void drawLightRequest(Light light, int posX, int posY) {
		for(int i = 0; i <= light.getDiameter(); i++) {
			drawLightLine(light, light.getRadius(), light.getRadius(), i, 0, posX, posY);
			drawLightLine(light, light.getRadius(), light.getRadius(), i, light.getDiameter(), posX, posY);
			drawLightLine(light, light.getRadius(), light.getRadius(), 0, i, posX, posY);
			drawLightLine(light, light.getRadius(), light.getRadius(), light.getDiameter(), i, posX, posY);
		}
	}
	
	private void drawLightLine(Light light, int xStart, int yStart, int xEnd, int yEnd, int posX, int posY) {
		//Algorithm - https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
		int dx = Math.abs(xEnd - xStart);
		int dy = Math.abs(yEnd - yStart);
		
		int sx = xStart < xEnd ? 1 : -1;
		int sy = yStart < yEnd ? 1: -1;
		
		int err = dx - dy;
		int e2;
		
		while(true) {
			int screenX = xStart - light.getRadius() + posX;
			int screenY = yStart - light.getRadius() + posY;
			
			if(screenX < 0 || screenX >= pW || screenY < 0 || screenY >= pH) { return; }

			int lightColour = light.getLightValue(xStart, yStart);
			if(lightColour == 0xff000000) { return; }
			
			if(lightBlock[screenX + screenY * pW] == Light.FULL) { return; }
			
			setLightMap(screenX, screenY, lightColour);
			
			if(xStart == xEnd && yStart == yEnd) { break; }
			
			e2 = 2 * err;
			
			if(e2 > -1 * dy) {
				err -= dy;
				xStart += sx;
			}
			else if(e2 < dx) {
				err += dx;
				yStart += sy;
			}
		}
	}
	
	public void setCurrentZDepth(int value) {
		this.currentZDepth = value;
	}
}

