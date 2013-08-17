package de.uniluebeck.iti.hanse.hansecontrol;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class PerformanceBenchmark {
	List<Long> values = new LinkedList<Long>();
	boolean isRunning = false;
	boolean firstStart = true;
	int runtime = 1000 * 60 * 5;
	String filename;
	Context context;	
	
	public static final boolean ENABLED = false;
	
	public PerformanceBenchmark(String filename, Context context) {
		this.filename = filename;
		this.context = context;
	}
	
	@SuppressWarnings("unused")
	public void log() {
		if (!ENABLED) {
			return;
		}
		if (firstStart) {
			firstStart = false;
			start();
			scheduleStop(5*60);
		}
		if (isRunning) {
			values.add(System.currentTimeMillis());
		}
	}
	
	private void writeToFile() {
		File extStorage = Environment.getExternalStorageDirectory();
		String path = extStorage.getAbsolutePath() + File.separator 
				+ MapManager.MAPS_DIR + File.separator + filename + ".benchmark";
		try {
			PrintWriter out = new PrintWriter(path);
			for (Long val : values) {
				out.println(val);
			}
			out.close();
			Log.d("Benchmark", filename + " finished!");
		} catch (Exception e) {
			Log.e("Benchmark", "Exception...", e);
		}
	}
	
	public void stop() {
		isRunning = false;
		MainScreen.getExecutorService().schedule(new Runnable() {
			
			@Override
			public void run() {
				writeToFile();
			}
		}, 5000, TimeUnit.MILLISECONDS);
	}
	
	public void start() {
		isRunning = true;
		Log.d("Benchmark", filename + " started!");
	}
	
	public void scheduleStop(int seconds) {
		MainScreen.getExecutorService().schedule(new Runnable() {
			
			@Override
			public void run() {
				stop();
			}
		}, seconds, TimeUnit.SECONDS);
	}
}
