package com.Bart.Engine.gfx;

public class Light {
	
	public static final int NONE = 0;
	public static final int FULL = 1;
	
	private int radius, diameter, colour;
	private int lightMap[];

	public Light(int radius, int colour) {
		this.radius = radius;
		this.colour = colour;
		this.diameter = radius * 2;
		
		lightMap = new int[diameter * diameter];
		
		for(int y = 0; y < diameter; y++) {
			for(int x = 0; x < diameter; x++) {
				double distance = Math.sqrt( ( (x - radius ) * (x - radius ) ) + ( ( y - radius ) * ( y - radius ) ) );
				
				if(distance < radius) {
					double power = 1 - (distance / radius);
					lightMap[x + y * diameter] = (int) ( ( ( colour >> 16 ) & 0xff ) * power ) << 16| (int) ( ( ( colour >> 8 ) & 0xff ) * power ) << 8 | (int) ( ( colour & 0xff ) * power );
				}
				else {
					lightMap[x + y * diameter] = 0xff000000;
				}
			}
		}
		
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getDiameter() {
		return diameter;
	}

	public void setDiameter(int diameter) {
		this.diameter = diameter;
	}

	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}

	public int[] getLightMap() {
		return lightMap;
	}

	public void setLightMap(int[] lightMap) {
		this.lightMap = lightMap;
	}
	
	public int getLightValue(int x, int y) {
		if(x < 0 || x >= diameter || y < 0 || y >= diameter) {
			return 0xff000000;
		}
		return lightMap[x + y * diameter];
	}
}
