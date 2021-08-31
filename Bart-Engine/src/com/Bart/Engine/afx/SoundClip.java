package com.Bart.Engine.afx;

import java.io.*;
import javax.sound.sampled.*;

public class SoundClip {
	
	private Clip clip;
	private FloatControl gainControl;
	
	public SoundClip(String path) {
		try {
			InputStream audioSource = SoundClip.class.getResourceAsStream(path);
			InputStream bufferedInput = new BufferedInputStream(audioSource); 
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInput);
			AudioFormat baseFormat = audioInputStream.getFormat();
			AudioFormat decodeFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
													   baseFormat.getSampleRate(),
													   16,
													   baseFormat.getChannels(),
													   baseFormat.getChannels() * 2,
													   baseFormat.getSampleRate(),
													   false);
			AudioInputStream audioInputStreamDecoded = AudioSystem.getAudioInputStream(decodeFormat, audioInputStream);
			
			clip = AudioSystem.getClip();
			clip.open(audioInputStreamDecoded);
			
			gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void play() {
		if(clip == null) {
			return;
		}
		
		stop();
		clip.setFramePosition(0);
		while(!clip.isRunning()) {
			clip.start();
		}
	}
	
	public void stop() {
		if(clip.isRunning()) {
			clip.stop();
		}
	}
	
	public void close() {
		stop();
		clip.drain();
		clip.close();
	}
	
	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		play();
	}
	
	public void setVolume(float value) {
		gainControl.setValue(value);
	}
	
	public boolean isRunning() {
		return clip.isRunning();
	}

}
