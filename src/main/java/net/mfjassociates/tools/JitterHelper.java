package net.mfjassociates.tools;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class JitterHelper implements Runnable {

	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static CompletableFuture<Long> cf = new CompletableFuture<Long>();
	private static Runnable task2 = () -> {
		long l=System.nanoTime();
		cf.complete(l);
	};
	private static final int DELAY = 3;
	private static final long DELAY_NANO = DELAY * 1000000000l;
	private static final DescriptiveStatistics stats=new DescriptiveStatistics();
	public static void main(String[] args) {
		JitterHelper jh=new JitterHelper();
		jh.run();
	}
	private static final String[] UNITS = new String[] {"nano","micro","milli","second"};
	private static String convert(long diff) {
		float lasti=1;
		int unitI=-1;
		for (long i = 1l; i < Math.abs(diff); i=i*1000) {
			lasti=i;
			unitI++;
		}
		if (unitI==-1) unitI=0; // if diff was 0, make sure it is in nano
		Float fdiff=diff/lasti;
		return fdiff.toString()+" "+UNITS[unitI];
	}
	@Override
	public void run() {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread( () -> {displayFinalStats();}));
			executor.scheduleAtFixedRate(task2, 3, 3, TimeUnit.SECONDS);
			long p=System.nanoTime();
			long c;
			long diff;
			while (true) {
				c=cf.get();
				diff=DELAY_NANO - (c-p);
				stats.addValue(diff);
				cf=new CompletableFuture<Long>();
				System.out.println(MessageFormat.format("Difference between ticks={0}, mean={1}, variance={2}, min={3}, max={4}", diff, stats.getMean(), stats.getVariance(), stats.getMin(), stats.getMax()));
				p=c;
			}
		} catch (InterruptedException | ExecutionException e) {
			displayFinalStats();
		}
	}
	private static void displayFinalStats() {
		System.out.println(MessageFormat.format("Stats: mean={1}, variance={2}, min={3}, max={4}", 0, stats.getMean(), stats.getVariance(), stats.getMin(), stats.getMax()));
	}
}
