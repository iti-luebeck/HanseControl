/*
* Copyright (c) 2015, Institute of Computer Engineering, University of Lübeck
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 
* * Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
* 
* * Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* 
* * Neither the name of the copyright holder nor the names of its
*   contributors may be used to endorse or promote products derived from
*   this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
