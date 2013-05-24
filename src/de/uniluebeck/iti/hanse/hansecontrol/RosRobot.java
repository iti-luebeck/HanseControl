package de.uniluebeck.iti.hanse.hansecontrol;

import geometry_msgs.PoseStamped;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.graphics.PointF;


/**
 * This class provides the global position and 
 * target paths for the controlled robot.
 * 
 * @author Stefan Hueske
 */
public class RosRobot implements MessageListener<geometry_msgs.PoseStamped> {
	
	private ConnectedNode node;
	
	Subscriber<geometry_msgs.PoseStamped> positionSubscriber;
	String positionTopic = "/hanse/position/estimate"; //TODO change static topic
	PointF lastPosition;
	
	private static RosRobot instance = new RosRobot();
	
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
	}
	
	
}
