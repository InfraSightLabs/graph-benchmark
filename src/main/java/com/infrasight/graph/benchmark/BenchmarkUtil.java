package com.infrasight.graph.benchmark;

import java.io.File;

/**
 * Some helper methods for graph benchmarks
 * 
 * @author Konrad Eriksson <konrad@infrasightlabs.com>
 *
 */
public class BenchmarkUtil {

	public static long getDirSize(File dir) {
		long tot = 0;
		for(File file : dir.listFiles()) {
			if(file.isDirectory())
				tot += getDirSize(file);
			else
				tot += file.length();
		}
		return tot;
	}
		
	public static void delDir(File dir) {
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) {
				delDir(file);
			}
			if(!file.delete())
				throw new RuntimeException("Failed to delete file: "+file);
		}
		if(!dir.delete())
			throw new RuntimeException("Failed to delete directory: "+dir);
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
