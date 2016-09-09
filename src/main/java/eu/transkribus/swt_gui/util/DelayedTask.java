package eu.transkribus.swt_gui.util;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Display;

public class DelayedTask {
	public static long DEFAULT_THRESH = 500;
	
	long lastTime = 0;
	Runnable task;
	final long timeThreshMs;
	boolean isGuiTask;
	
	public DelayedTask(Runnable task, boolean isGuiTask) {
		this(task, isGuiTask, DEFAULT_THRESH);
	}
	
	public DelayedTask(Runnable task, boolean isGuiTask, long threshMs) {
		this.task = task;
		this.isGuiTask = isGuiTask;
		this.timeThreshMs = threshMs;
	}
	
	public void start() {
		lastTime = System.currentTimeMillis();
		final long DIFF_T = 500;
		new Timer().schedule(new TimerTask() {
			@Override public void run() {
				long selDiff = System.currentTimeMillis() - lastTime;
				if (selDiff >= timeThreshMs) {
					if (isGuiTask)
						Display.getDefault().asyncExec(task);
					else
						task.run();
				}
			}
		}, DIFF_T);
	}

}
