package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.LinkedList;
import java.util.List;

import geometry_msgs.PoseStamped;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.graphics.PointF;


/**
 * This class provides the global position and 
 * target paths for the controlled robot.
 * The App will be subscribed to the topics used here at all times.
 * 
 * @author Stefan Hueske
 */
public class RosRobot implements MessageListener<geometry_msgs.PoseStamped> {
	
	private ConnectedNode node;
	
	Subscriber<geometry_msgs.PoseStamped> positionSubscriber;
	String positionTopic = "/hanse/position/estimate"; //TODO change static topic
	PointF lastPosition;
	
	private static RosRobot instance = new RosRobot();
	
	List<RobotUpdateListener> robotUpdateListeners = new LinkedList<RobotUpdateListener>();
	
	public static RosRobot getInstance() {
		return instance;
	}
	
	public void setNode(ConnectedNode connectedNode) {
		this.node = connectedNode;
		positionSubscriber = node.newSubscriber(positionTopic, geometry_msgs.PoseStamped._TYPE);
		positionSubscriber.addMessageListener(this);
	}

	public PointF getPosition() {
		return lastPosition;
	}

	@Override
	public void onNewMessage(PoseStamped pose) {
		geometry_msgs.Point p = pose.getPose().getPosition();
		lastPosition = new PointF((float)p.getX(), (float)p.getY());
		notifyUpdateListeners();
	}
	
	private void notifyUpdateListeners() {
		synchronized (robotUpdateListeners) {
			for (final RobotUpdateListener listener : robotUpdateListeners) {
				MainScreen.getExecutorService().execute(new Runnable() {
					@Override
					public void run() {
						listener.update();
					}
				});
			}
		}
	}
	
	public interface RobotUpdateListener {
		public void update();
	}
	
	public void addRobotUpdateListener(RobotUpdateListener listener) {
		synchronized (robotUpdateListeners) {			
			robotUpdateListeners.add(listener);
		}
	}
}
