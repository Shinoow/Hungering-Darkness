package com.shinoow.hungeringdarkness.common.cap;

public interface IDarknessTimerCapability {

	public void incrementTimer();

	public void decrementTimer();

	public int getTimer();

	public void setTimer(int timer);

	public void copy(IDarknessTimerCapability cap);
}
