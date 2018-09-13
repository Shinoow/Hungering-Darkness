package com.shinoow.hungeringdarkness.common.cap;

public class DarknessTimerCapability implements IDarknessTimerCapability {

	private int timer;

	@Override
	public void incrementTimer(){
		timer += 1;
	}

	@Override
	public void decrementTimer(){
		timer -= 1;
		if(timer < 0) timer = 0;
	}

	@Override
	public int getTimer(){
		return timer;
	}

	@Override
	public void setTimer(int timer) {
		this.timer = timer;
	}

	@Override
	public void copy(IDarknessTimerCapability cap) {
		timer = cap.getTimer();
	}
}