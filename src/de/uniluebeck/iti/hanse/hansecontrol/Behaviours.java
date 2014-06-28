package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashSet;
import java.util.Set;

import geometry_msgs.Twist;
import hanse_msgs.BehaviourStatus;

import org.ros.internal.message.DefaultMessageFactory;
import org.ros.internal.message.RawMessage;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import std_msgs.Header;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer.BehavioursListener;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer.IBehaviours;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer.Target;

public class Behaviours implements MessageListener<hanse_msgs.BehaviourStatus>, IBehaviours {
	
	private ConnectedNode node;
	
	Set<String> behaviours = new HashSet<String>();
	
	BehavioursListener behavioursListener;
	
	Publisher<hanse_msgs.BehaviourStatus> pubBehaviour;
	Publisher<geometry_msgs.PoseStamped> pubGoal;
	Subscriber<hanse_msgs.BehaviourStatus> subBehaviours;
	
	static Behaviours instance = new Behaviours();
	
//	String currentBehaviour = "";
	
	
	public static Behaviours getInstance() {
		return instance;
	}
	
	@Override
	public void onNewMessage(BehaviourStatus msg) {
		String name = msg.getName().getData();
		String status = msg.getStatus().getData();
		behaviours.add(name);
//		if (status.equals("finished")) {
//			Log.d("Behaviours", "finished: " + name + " curr:" + currentBehaviour);
//		}
		if (status.equals("finished")) {
			Log.d("Behaviours", "behaviourFinished(" + name + ")");
			behavioursListener.behaviourFinished(name);
//			currentBehaviour = "";
		}
	}
	
	public Set<String> getBehaviours() {
		return behaviours;
	}

	@Override
	public void setNode(ConnectedNode node) {
		this.node = node;
		behaviours.clear();
		pubBehaviour = node.newPublisher("/hanse/behaviourstatus", hanse_msgs.BehaviourStatus._TYPE);
		pubGoal = node.newPublisher("/goal", geometry_msgs.PoseStamped._TYPE);
		subBehaviours = node.newSubscriber("/hanse/behaviourstatus", hanse_msgs.BehaviourStatus._TYPE);
//		subBehaviours.addMessageListener(this, MainScreen.MESSAGE_QUEUE);
	}

	@Override
	public void sendGoal(Target target) {
		if (node != null && pubGoal != null) {
			Log.d("Behaviours", "sendGoal(" + target.getRosPos().toString() + ")");
			PointF rosPos = target.getRosPos();
			lastTarget = rosPos;
			geometry_msgs.PoseStamped goal = pubGoal.newMessage();
			
			geometry_msgs.Pose pose = node.getTopicMessageFactory()
					.newFromType(geometry_msgs.Pose._TYPE);
			geometry_msgs.Point point = node.getTopicMessageFactory()
					.newFromType(geometry_msgs.Point._TYPE);
			point.setX(rosPos.x);
			point.setY(rosPos.y);
			point.setZ(0);
			pose.setPosition(point);
			goal.setPose(pose);
			std_msgs.Header header = node.getTopicMessageFactory()
					.newFromType(std_msgs.Header._TYPE);
			header.setFrameId("/map");
			goal.setHeader(header);
			
			pubGoal.publish(goal);			
		}
	}

	@Override
	public void setBehavioursListener(BehavioursListener behavioursListener) {
		this.behavioursListener = behavioursListener;
	}

	@Override
	public void startBehaviour(String behName) {
		if (node != null && pubBehaviour != null) {
			Log.d("Behaviours", "startBehaviour(" + behName + ")");
			hanse_msgs.BehaviourStatus msg = pubBehaviour.newMessage();
			std_msgs.String name = node.getTopicMessageFactory().newFromType(std_msgs.String._TYPE);
			name.setData(behName);
			std_msgs.String status = node.getTopicMessageFactory().newFromType(std_msgs.String._TYPE);
			status.setData("start");
			msg.setName(name);
			msg.setStatus(status);
			pubBehaviour.publish(msg);
//			currentBehaviour = behName;
		}
	}
	
	//-----------Target-reached-workaround----------------------------------------------
	RosRobot rosRobot;
	PointF lastTarget;
	
	private Behaviours() {
		rosRobot = RosRobot.getInstance();
		rosRobot.addRobotPositionListener(new RosRobot.RobotPositionListener() {
			
			@Override
			public void positionUpdate() {
				if (lastTarget != null && distance(rosRobot.getPosition(), lastTarget) < 1) {
					Log.d("Behaviours", "goalReached()");
					lastTarget = null;
					behavioursListener.goalReached();
				}
			}
		});
	}
	
	private double distance(PointF p1, PointF p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
	
}
