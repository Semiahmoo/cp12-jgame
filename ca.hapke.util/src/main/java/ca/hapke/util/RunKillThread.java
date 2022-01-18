package ca.hapke.util;

/**
 * @author Mr. Hapke
 */
public abstract class RunKillThread extends Thread {

	protected boolean kill = false;
	private boolean running = false;
	private boolean finished = false;

	@Override
	public final void run() {
		running = true;
		doWork();
		running = false;
		finished = true;
	}

	public void kill() {
		this.kill = true;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isFinished() {
		return finished;
	}

	protected abstract void doWork();
}