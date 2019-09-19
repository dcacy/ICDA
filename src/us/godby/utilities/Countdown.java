package us.godby.utilities;

public class Countdown {
	
	private int ticks = 0;
	private long sleep = 0;
	private long interval = 0;
	private long remaining = 0;
	
	public Countdown(int ticks, long sleep) {
		this.ticks = ticks;
		this.sleep = sleep;
		this.remaining = sleep;
		this.interval = this.sleep / this.ticks;
	}
	
	public void start() {
		while (ticks > 0) {
			System.out.println("    Retrying API request in " + (remaining / 1000) + " seconds.");
            remaining -= interval;
			ticks--;
			
			try { Thread.sleep(interval); } catch (Exception e) {}
		}
	}
	
}