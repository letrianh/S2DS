package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

public class Player extends AbstractDevice {
	
	static int DEFAULT_SPEED = 30;
	
	public PlayerStatus getStatus() {
		return (PlayerStatus) status;
	}
	
	Player(String name, String ch, ArrayList<DeviceStatus> l) {
		this(name, ch, l, DEFAULT_SPEED);
	}
	
	Player(String name, String ch, ArrayList<DeviceStatus> l, int s) {
		super(name, ch, new PlayerStatus(), l);
		type = DeviceTypes.PLAYER;
		getStatus().speed = s;
	}
	
	public void setSpeed(int n) {
		getStatus().speed = n;
	}

	public void search(int n) {
		getStatus().position = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}

	public void still() {
		if (getStatus().isPlaying) {
			getStatus().position += (getStatus().atTime - getStatus().prevTime)*getStatus().speed;
			getStatus().isPlaying = false;
			getStatus().state = DeviceState.STABLE;
			this.recordStatus();
		}
		// ignore STILL if isPlaying==false
	}

	public void play() {
		if (!getStatus().isPlaying) {
			getStatus().isPlaying = true;
			getStatus().state = DeviceState.STABLE;
			this.recordStatus();
		}
		// ignore PLAY if isPlaying==true
	}

	public void play(int n) {
		if (!getStatus().isPlaying) {
			getStatus().isPlaying = true;
			getStatus().state = DeviceState.STABLE;
			this.recordStatus();
			getStatus().atTime += (n - getStatus().position)/getStatus().speed; 
			getStatus().position = n;
			this.recordStatus();
		}
		// ignore PLAY if isPlaying==true
	}
}
