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
	Publisher<geometry_msgs.Twist> pubGoal;
	
	@Override
	public void onNewMessage(BehaviourStatus msg) {
		String name = msg.getName().getData();
		String status = msg.getStatus().getData();
		behaviours.add(name);
		if (status.equals("finished")) {
			Log.d("Behaviours", "behaviourFinished(" + name + ")");
			behavioursListener.behaviourFinished(name);
		}
	}
	
	public Set<String> getBehaviours() {
		return behaviours;
	}

	@Override
	public void setNode(ConnectedNode node) {
		this.node = node;
		pubBehaviour = node.newPublisher("/hanse/BehaviourStatus", hanse_msgs.BehaviourStatus._TYPE);
		pubGoal = node.newPublisher("/goal", geometry_msgs.Twist._TYPE);
	}

	@Override
	public void sendGoal(Target target) {
		if (node != null && pubGoal != null) {
			Log.d("Behaviours", "sendGoal(" + target.getRosPos().toString() + ")");
			PointF rosPos = target.getRosPos();
			lastTarget = rosPos;
			geometry_msgs.Twist goal = pubGoal.newMessage();
			geometry_msgs.Vector3 vec3 = node.getTopicMessageFactory()
					.newFromType(geometry_msgs.Vector3._TYPE);
			vec3.setX(rosPos.x);
			vec3.setY(rosPos.y);
			vec3.setZ(0);
			goal.setLinear(vec3);
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
		}
	}
	
	//-----------Target-reached-workaround----------------------------------------------
	RosRobot rosRobot;
	PointF lastTarget;
	
	public Behaviours() {
		rosRobot = RosRobot.getInstance();
		rosRobot.addRobotPositionListener(new RosRobot.RobotPositionListener() {
			
			@Override
			public void positionUpdate() {
				if (lastTarget != null && distance(rosRobot.getPosition(), lastTarget) < 10) {
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
