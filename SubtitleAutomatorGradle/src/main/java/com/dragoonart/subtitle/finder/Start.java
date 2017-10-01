package com.dragoonart.subtitle.finder;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Start {

	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
	
	public static void main(String[] args) {
		TimedFileScanner fScanner = new TimedFileScanner("E:/Downloads");
		 scheduler.scheduleAtFixedRate(fScanner, 0, 5, TimeUnit.SECONDS);	
	}
}