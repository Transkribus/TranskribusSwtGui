package org.dea.util;

import java.util.TimerTask;

public class DelayTimerTask extends TimerTask {
	public long lastTime;
	long delay;
	
	public DelayTimerTask(long delay) {
		this.delay = delay;
	}

	@Override public void run() {
	}
	
	

}
