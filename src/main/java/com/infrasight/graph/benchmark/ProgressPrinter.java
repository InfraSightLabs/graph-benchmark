package com.infrasight.graph.benchmark;

/**
 * Progress printer throttled to 1 msg/sec.
 * 
 * @author Konrad Eriksson <konrad@infrasightlabs.com>
 *
 */
public class ProgressPrinter {
	final long total;
	final String unit;
	
	final long startTime;
	long lastTime = 0;
	int lastValue = -1;
	long lastPos = 0;
	
	public ProgressPrinter(long total, String unit) {
		this.total = total;
		this.unit = unit;
		this.startTime = System.currentTimeMillis();
	}

	public void report(long pos) {
		int newValue = (int) ((pos * 100) / total);
		if(newValue <= lastValue)
			return;
		
		lastValue = newValue;
		
		long cur = System.currentTimeMillis();
		if(cur < lastTime + 1000)
			return;
		
		System.out.println("Progress "+newValue+"% done ("+(pos - lastPos)*1000L/((cur-lastTime))+" "+unit+"/sec)");
		lastTime = cur;
		lastPos = pos;
	}
	
	public void done(String action) {
		long cur = System.currentTimeMillis();
		System.out.println(action+" "+total+" in "+(cur-startTime)+"ms ("+total*1000L/((cur-startTime))+" "+unit+"/sec)");
	}

	public void done(String action, long size) {
		long cur = System.currentTimeMillis();
		System.out.println(action+" "+total+" in "+(cur-startTime)+"ms ("+total*1000L/((cur-startTime))+
				" "+unit+"/sec, disk size: "+BenchmarkUtil.humanReadableByteCount(size, false)+")");
	}
	
}
