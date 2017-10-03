package com.dragoonart.subtitle.finder;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Start {

	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static void main(String[] args) {
		TimedFileScanner fScanner = new TimedFileScanner("E:/Downloads/Ghost.In.The.Shell.2017.1080p.HC.HDRip.X264.AC3-EVO");
		fScanner.run();
//		 scheduler.scheduleAtFixedRate(fScanner, 0, 5, TimeUnit.SECONDS);	E:\Downloads\Ghost.In.The.Shell.2017.1080p.HC.HDRip.X264.AC3-EVO
	}
}
