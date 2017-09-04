package examples;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUsage {
	private final static Logger logger = LoggerFactory.getLogger(MemoryUsage.class);
	
	public static void printMemoryUsage() {
		/* Total amount of free memory available to the JVM */
		System.out.println("Free memory (MB): " + Runtime.getRuntime().freeMemory() / 1024 / 1024 );

		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		System.out.println("Maximum memory (MB): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / 1024 / 1024 ));

		/* Total memory currently in use by the JVM */
		System.out.println("Total memory (MB): " + Runtime.getRuntime().totalMemory() / 1024 / 1024 );
	}

	public static void main(String[] args) {
		/* Total number of processors or cores available to the JVM */
		System.out.println("Available processors (cores): " + Runtime.getRuntime().availableProcessors());

		/* Total amount of free memory available to the JVM */
		System.out.println("Free memory (bytes): " + Runtime.getRuntime().freeMemory() / 1024 / 1024 );

		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		System.out.println("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / 1024 / 1024 ));

		/* Total memory currently in use by the JVM */
		System.out.println("Total memory (bytes): " + Runtime.getRuntime().totalMemory() / 1024 / 1024 );

		/* Get a list of all filesystem roots on this system */
		File[] roots = File.listRoots();

		/* For each filesystem root, print some info */
		for (File root : roots) {
			System.out.println("File system root: " + root.getAbsolutePath());
			System.out.println("Total space (bytes): " + root.getTotalSpace());
			System.out.println("Free space (bytes): " + root.getFreeSpace() );
			System.out.println("Usable space (bytes): " + root.getUsableSpace() );
		}
	}
	
}
