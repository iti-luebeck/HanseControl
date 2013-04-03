package de.uniluebeck.iti.hanse.hansecontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.ros.address.InetAddressFactory;
import org.ros.android.NodeMainExecutorService;
import org.ros.android.RosActivity;
import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

public class ChatterActivity extends RosActivity {

	private Talker talker;
	private Listener listener;
	
	TextView history;
	
	public ChatterActivity() {
		super("HanseControl", "HanseControl");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatter);
		final Button sendbutton = (Button) findViewById(R.id.sendButton);
		history = (TextView) findViewById(R.id.history);
		final TextView sendtext = (TextView) findViewById(R.id.messageText);
		// final ScrollView historyScroll = (ScrollView)
		// findViewById(R.id.scrollView1);

//		history.setText("s0\ns1\ns2\ns3\ns4\ns5\ns6\ns7\ns8\ns9\ns10\ns11\ns12\ns13\ns14\ns15\ns16\ns17\ns18\ns19\ns20\ns21\ns22\ns23\ns24\ns25\ns26\ns27\ns28\ns29\ns30\ns31\ns32\ns33\ns34\ns35\ns36\ns37\ns38\ns39\n");
		history.setKeyListener(null);
		sendtext.setText("Hello World!");
		sendbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CharSequence text = sendtext.getText();
//				history.setText(text + "\n" + history.getText());
				sendtext.setText("");
				std_msgs.String rosmsg = talker.getPublisher().newMessage();
				rosmsg.setData(text.toString());
				talker.getPublisher().publish(rosmsg);
			}
		});
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

	@Override
	protected void init(NodeMainExecutor nodeMainExecutor) {
		
		talker = new Talker();
		listener = new Listener() {
			
			@Override
			public void messageReceived(final String rosStr) {
				history.post(new Runnable() {
					
					@Override
					public void run() {
						history.setText(history.getText() + "\n" + rosStr.getData());
					}
				});
			}
		};
		
//		listener.getSubscriber().addMessageListener(new MessageListener<std_msgs.String>() {
//			@Override
//			public void onNewMessage(final std_msgs.String message) {
//				messageReceived(message);
//			}
//		});
		
		NodeConfiguration nodeConfiguration = NodeConfiguration
				.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
		// NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
		// try {
		nodeConfiguration.setMasterUri(getMasterUri()); // TODO get master uri
														// via settings
		// } catch (URISyntaxException e) {}
		nodeMainExecutor.execute(talker, nodeConfiguration);
		nodeMainExecutor.execute(listener, nodeConfiguration);
	}
}

class Talker extends AbstractNodeMain {
	Publisher<std_msgs.String> publisher;
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("map_screen/talker");
	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {
		publisher = connectedNode
				.newPublisher("chatter", std_msgs.String._TYPE);
		// This CancellableLoop will be canceled automatically when the node
		// shuts
		// down.
//		connectedNode.executeCancellableLoop(new CancellableLoop() {
//			private int sequenceNumber;
//
//			@Override
//			protected void setup() {
//				sequenceNumber = 0;
//			}
//
//			@Override
//			protected void loop() throws InterruptedException {
//				std_msgs.String str = publisher.newMessage();
//				str.setData("Hello world! " + sequenceNumber);
//				publisher.publish(str);
//				Log.w("ros_mylog", "Publishing String: " + str); // TODO change
//																	// tag
//				sequenceNumber++;
//				Thread.sleep(1000);
//			}
//		});
	}
	
	public Publisher<std_msgs.String> getPublisher() {
		return publisher;
	}
}

abstract class Listener extends AbstractNodeMain {

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava_tutorial_pubsub/listener");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		// final org.apache.commons.logging.Log log = connectedNode.getLog();
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(
				"chatter", std_msgs.String._TYPE);
		subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message) {
//				log.info("I heard: \"" + message.getData() + "\"");
				Log.w("ros_mylog", "Ros message received! '" + message.getData() + "'"); //TODO change log tag
				messageReceived(message);
			}
		});
		
	}
	
	public abstract void messageReceived(std_msgs.String rosStr);
	
}
