package com.Bart.Game;

import com.Bart.Engine.AbstractGame;
import com.Bart.Engine.GameContainer;
import com.Bart.Engine.Renderer;
import com.Bart.Engine.afx.SoundClip;
import com.Bart.Engine.gfx.*;

public class GameManager extends AbstractGame {
	
	private Image image;
	private Image image2;
	private Light light;
	private SoundClip music;
	
	public GameManager() {
		image = new Image("/Images/Test.png");
		image.setLightBlock(Light.FULL);
		image2 = new Image("/Images/WhiteBackground.png");
		light = new Light(100, 0xff00ffff);
	}

	@Override
	public void update(GameContainer gc, float dt) {

	}

	@Override
	public void render(GameContainer gc, Renderer r) {
		r.drawLight(light,gc.getInput().getMouseX(), gc.getInput().getMouseY());
		r.drawImage(image2, 0, 0);
		r.drawImage(image, 100, 100);
	}
	
	public static void main(String args[]) {
		GameContainer gc = new GameContainer(new GameManager());
		gc.start();
	
	}

}
