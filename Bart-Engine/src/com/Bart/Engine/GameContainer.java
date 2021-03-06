package com.Bart.Engine;

import java.awt.event.*;

public class GameContainer implements Runnable{
	
	private Thread thread;
	private Window window;
	private Renderer renderer;
	private Input input;
	
	private AbstractGame game;
	
	private boolean running = false;
	private final double UPDATE_CAP = 1.0 / 60.0; //60 FPS 
	
	private int width = 320, height = 240;
	private float scale = 3f;
	private String title = "Bart's Engine";

	public GameContainer(AbstractGame game) { 
		this.game = game;
	}
	
	public void start() {
		window = new Window(this);
		renderer = new Renderer(this);
		input = new Input(this);
		
		thread = new Thread(this);
		thread.run(); // .run() for main thread .start() for side thread
	}
	
	public void stop() {
		
	}
	
	public void run() {
		running = true;
		
		boolean render = false;
		double currentTime;
		double lastTime = System.nanoTime() / 1000000000.0; //current nanoTime of the system. It is extremely accurate so we divide it by 1000000000.0
		double timePassed = 0.0;
		double unprocessedTime = 0.0;
		
		double frameTime = 0.0;
		int framesPerSecond = 0;
		int frames = 0;
		
		while(running) {
			render = false;
			
			currentTime = System.nanoTime() / 1000000000.0; //current time
			timePassed = currentTime - lastTime; //the length of time that passed since lastTime
			lastTime = currentTime;
			
			unprocessedTime += timePassed;
			frameTime += timePassed;
			
			while(unprocessedTime >= UPDATE_CAP) { //if we miss updates we still want to do those updates that we missed
				unprocessedTime -= UPDATE_CAP;
				
				render = true;
				
				game.update(this, (float) UPDATE_CAP);
			
				input.update();
				
				if(frameTime >= 1.0) { //1 second
					frameTime = 0.0;
					framesPerSecond = frames;
					frames = 0;
				}
			}
			
			if(render) {
				renderer.clear();
				
				game.render(this, renderer);
				renderer.process();
				renderer.drawText("FPS: " + framesPerSecond, 0, 0, 0xffe3092f);
				window.update();
				frames++;
			}
			else {
				try {
					thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		dispose();
	}
	
	private void dispose() {
		
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Window getWindow() {
		return window;
	}

	public Input getInput() {
		return input;
	}

}
