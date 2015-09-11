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
package de.uniluebeck.iti.hanse.hansecontrol.rosbackend;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.ros.address.InetAddressFactory;
import org.ros.android.NodeMainExecutorService;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen.NodeConnectedListener;
import de.uniluebeck.iti.hanse.hansecontrol.R.drawable;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

//workaround to get access to NodeMainExecutorService
public class RosService extends NodeMainExecutorService {
	LocalBinder binder;
    
	Handler handler = new Handler();
	
	static final String ACTION_START = "org.ros.android.ACTION_START_NODE_RUNNER_SERVICE";
	static final String ACTION_SHUTDOWN = "org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";
	static final String EXTRA_NOTIFICATION_TITLE = "org.ros.android.EXTRA_NOTIFICATION_TITLE";
	static final String EXTRA_NOTIFICATION_TICKER = "org.ros.android.EXTRA_NOTIFICATION_TICKER";
	
	List<RosServiceListener> listeners = Lists.newLinkedList();

	public static final int DISCONNECTED = 0;
	public static final int CONNECTED = 1;
	public static final int IN_PROGRESS = 2;
	
	private int connectionState = DISCONNECTED;
	
	private ConnectedNode connectedNode;
	
	RosDataProvider rosDataProvider;

	public static final int MESSAGE_QUEUE = 20;
	
	public RosService() {
		super();
		handler = new Handler();
		binder = new LocalBinder(this);
		rosDataProvider = new RosDataProvider(this);
	}

	public static class LocalBinder extends Binder {
		RosService service;
		
		public LocalBinder(RosService service) {
			this.service = service;
		}
		
		public RosService getService() {
			return service;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public synchronized void shutdown() {
		Log.d("CustomNodeMainExecutorService", "shutdown()");
		for (RosServiceListener listener : listeners) {
			listener.onShutdown();
		}
		super.shutdown();
	}

	public int getConnectionState() {
		return connectionState;
	}
	
	public RosDataProvider getRosDataProvider() {
		return rosDataProvider;
	}
	
	public synchronized void addRosServiceListener(
			RosServiceListener listener) {
		listeners.add(listener);
		if (connectionState == CONNECTED) {
			//service was already connected, inform listener
			listener.onConnected();
		}
	}

	public static interface RosServiceListener {
		public void onConnected();
		public void onShutdown();
		public void onError();
	}
	
	public ConnectedNode getConnectedNode() {
		return connectedNode;
	}

	private static final int ONGOING_NOTIFICATION = 1;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction() == null) {
			return START_NOT_STICKY;
		}
		
		if (intent.getAction().equals(ACTION_START)) {
			Preconditions.checkArgument(intent
					.hasExtra(EXTRA_NOTIFICATION_TICKER));
			Preconditions.checkArgument(intent
					.hasExtra(EXTRA_NOTIFICATION_TITLE));
			Notification notification = new Notification(R.drawable.icon,
					intent.getStringExtra(EXTRA_NOTIFICATION_TICKER),
					System.currentTimeMillis());
			Intent notificationIntent = new Intent(this,
					RosService.class);
			notificationIntent.setAction(ACTION_SHUTDOWN);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(this,
					intent.getStringExtra(EXTRA_NOTIFICATION_TITLE),
					"Tap to shutdown.", pendingIntent);
			startForeground(ONGOING_NOTIFICATION, notification);
		}
		
		if (intent.getAction().equals(ACTION_SHUTDOWN)) {
			shutdown();
		}
		
		return START_NOT_STICKY;
	}

	public synchronized void connect(String masterUri) throws RosConnectionException {
		if (connectionState == CONNECTED || connectionState == IN_PROGRESS) {
			Log.d("RosService", "connect() ignored, already connected or in progress");
			return;
		}
		try {
			URI uri = new URI(masterUri);
			if (!uri.isAbsolute()) {
				throw new Exception();
			}
			setMasterUri(uri);
			connectionState = IN_PROGRESS;
			Log.d("RosService", "creating and executing node...");
			NodeConfiguration nodeConfiguration = NodeConfiguration
					.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
			nodeConfiguration.setMasterUri(getMasterUri());
			execute(new Node(), nodeConfiguration);
		} catch (Exception e) {
			connectionState = DISCONNECTED;
			for (RosServiceListener listener : listeners) {
				listener.onError();
			}
		}
	}
	
	
	public static class RosConnectionException extends Exception { 
		public RosConnectionException() { }
		public RosConnectionException(String msg) {
			super(msg); 
		}
	}
	
	private class Node extends AbstractNodeMain {

		@Override
		public GraphName getDefaultNodeName() {
			return GraphName.of("hanse_control");
		}

		@Override
		public void onStart(ConnectedNode node) {
			connectedNode = node;
			connectionState = CONNECTED;
			Log.d("RosService", "node connected");
			for (RosServiceListener listener : listeners) {
				listener.onConnected();
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RosService.this, "ROS connection established", Toast.LENGTH_LONG).show();					
				}
			});
			
//			executorService.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					synchronized (nodeConnectedListeners) {
//						for (NodeConnectedListener listener : nodeConnectedListeners) {
//							listener.onNodeConnected(node);
//						}
//					}
//				}
//			});

		}

		@Override
		public void onError(org.ros.node.Node node, Throwable e) {
			Log.e("RosService", "onError", e);
			connectionState = DISCONNECTED;
			for (RosServiceListener listener : listeners) {
				listener.onError();
			}
		}
	}

	public boolean isConnected() {
		return getConnectionState() == CONNECTED;
	}
	
}
// TODO remove class