package de.uniluebeck.iti.hanse.hansecontrol.rosbackend;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.ros.android.NodeMainExecutorService;
import org.ros.android.NodeMainExecutorServiceListener;
import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.topic.Subscriber;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.content.Context;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.TreeValue;

public class RosDataProvider {
	RosService service;
	
	ExecutorService executorService = Executors.newCachedThreadPool();
	Set<String> nonRosMethods;
	
	Map<Integer, DefaultRosDataConnection> connections = Maps.newHashMap();
	
	public RosDataProvider(RosService service) {
		this.service = service;
		service.addRosServiceListener(new RosService.RosServiceListener() {
			
			@Override
			public void onShutdown() {
				executorService.shutdownNow();
			}
			
			@Override
			public void onError() {
				
			}
			
			@Override
			public void onConnected() {
				
			}
		});
		
		nonRosMethods = new HashSet<String>();
		nonRosMethods.add("getClass");
		nonRosMethods.add("getHeader");
		nonRosMethods.add("getInstance");
		nonRosMethods.add("getInvocationHandler");
		nonRosMethods.add("getProxyClass");
	}
	
//	public static void testTopicDiscovery() throws Exception {
//		Log.e("topicdiscoveryTest", "t3");
//		XmlRpcClient client = new XmlRpcClient();
//		XmlRpcClientConfigImpl clientConfig = new XmlRpcClientConfigImpl();
//		clientConfig.setServerURL(new URL("http://ROS:11311"));
//		Object result = client.execute(clientConfig, "getPublishedTopics", 
//				new String[]{"/hansecontroltopiclookup", ""});
//		for (Object o : (Object[])((Object[])result)[2]) {
//			Log.d("topicdiscoveryTest", "Topic: " + ((Object[])o)[0] + " Type: " + ((Object[])o)[1]);
//		}
//		Log.d("topicdiscoveryTest", "Done");
//	}
	
//	public void testPose(Message msg) throws Exception {
//		Class<?> cl = msg.getClass();
//		Method[] methods = cl.getMethods();
////		List<Method> getters = new LinkedList<Method>();
//		
//		for (Method m : methods) {
////			Log.d("reflectionTest", "Method: " + m.getName());
//			if (m.getName().startsWith("get") && !nonRosMethods.contains(m.getName())) {
////				getters.add(m);
//				Object data = m.invoke(msg, new Object[0]);
//				Log.d("reflectionTest", "InnerObject: " + m.getName() + " of type: " + data.getClass().getName());
//				if (data instanceof Message) {
//					testPose((Message)data);
//				}
//			}
//		}
//		
//	}
	
	private void parseTopicHierarchy(Topic parentTopic, Class parentClass) throws Exception {
//		Log.d("topicdiscoveryTest", "Type:" + parentTopic.type + " AttributPath: " + parentTopic.getAttributPath());
		Method[] methods = parentClass.getMethods();
		for (Method m : methods) {
//			if (m.getName().equals("getX")) {
//				m.getReturnType();
//			}
			if (m.getName().startsWith("get") && !nonRosMethods.contains(m.getName())) {
				Class mCl = m.getReturnType();
				if (Arrays.asList(mCl.getInterfaces()).contains(Message.class) 
						|| mCl.isPrimitive()) {
					Topic t = new Topic(parentTopic.topic, mCl.getName());
					t.setRootType(parentTopic.getRootType());
					LinkedList<String> aP = new LinkedList<String>(parentTopic.getAttributePath());
					aP.add(m.getName());
					t.setAttributPath(aP);
					parentTopic.getSubTopics().add(t);
					Log.d("topicdiscoveryTest", "Type:" + t.type + " AttributPath: " + t.getAttributePath());
					if (!mCl.isPrimitive()) {
						parseTopicHierarchy(t, mCl);
					}
				}
			}
		}
	}
	
	private List<Topic> convertRawRpcToTopics(Object data) {
		List<Topic> res = new LinkedList<RosDataProvider.Topic>();
		
		for (Object o : (Object[])((Object[])data)[2]) {
			Log.d("topicdiscoveryTest", "Topic: " + ((Object[])o)[0] + " Type: " + ((Object[])o)[1]);
			String rootType = ((Object[])o)[1].toString();
			Topic rootTopic = new Topic(((Object[])o)[0].toString(), rootType.replace("/", "."));
			rootTopic.setRootType(rootType);
//			List<String> attributePath = new LinkedList<String>();
			rootTopic.setAttributPath(new LinkedList<String>()); //empty for root topic
//			List<Topic> subTopics = new LinkedList<Topic>();
			
			try {
				parseTopicHierarchy(rootTopic, Class.forName(rootTopic.getType()));
			} catch (Exception e) {
				Log.e("RosDataProvider", "Error while parsing Type Tree", e);
			}
			
//			rootTopic.setSubTopics(subTopics);
			res.add(rootTopic);
		}
		Log.e("topicdiscoveryTest", "Done");
		
		return res;
	}
	
	public void getAvailableTopics(final TopicsResultCallback callback) {
		if (!service.isConnected()) {
			return;
		}
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					XmlRpcClient client = new XmlRpcClient();
					XmlRpcClientConfigImpl clientConfig = new XmlRpcClientConfigImpl();
					clientConfig.setServerURL(service.getMasterUri().toURL());
					Object result = client.execute(clientConfig, "getPublishedTopics", 
							new String[]{"/hansecontroltopiclookup", ""});
					callback.resultCallback(convertRawRpcToTopics(result));
				} catch (Exception e) {
					Log.e("RosDataProvider", "getAvailableTopics() error", e);
				}
			}
		});
	}
	
	public RosDataConnection register(final int id, Topic topic) throws RegistrationException {
		Log.d("rosconerr", "register():" + id);
		if (service.getConnectionState() != RosService.CONNECTED) {
			throw new RegistrationException("RosService is not connected!");
		}
		DefaultRosDataConnection con = connections.get(id);
		if (con == null) {
			Subscriber<?> subscriber = service.getConnectedNode().newSubscriber(topic.getTopicName(), topic.getRootType());
			con = new DefaultRosDataConnection(id, subscriber, topic);
			connections.put(id, con);
		}
		return con;
	}
	
	private class DefaultRosDataConnection implements RosDataConnection, MessageListener {

		int id;
		Subscriber<?> subscriber;
		RosDataUpdateListener updateListener;
		Topic topic;
		
		public DefaultRosDataConnection(int id, Subscriber<?> subscriber, Topic topic) {
			this.id = id;
			this.subscriber = subscriber;
			this.topic = topic;
			subscriber.addMessageListener(this);
		}
		
		@Override
		public void setUpdateListener(RosDataUpdateListener updateListener) {
			this.updateListener = updateListener;
		}

		@Override
		public void removeUpdateListener() {
			updateListener = null;
		}
		
		@Override
		public void shutdown() {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					connections.remove(id);
					subscriber.shutdown();
				}
			});
		}
		
		public RosDataUpdateListener getUpdateListener() {
			return updateListener;
		}

		@Override
		public void onNewMessage(Object data) {
			//TODO cache data if no listener is available
			if (updateListener != null) {
				updateListener.update(parseAttribute(topic, data));
			}
		}		
	}
	
	private Object parseAttribute(Topic topic, Object data) {
		Object res = null;
		try {
			Object currObj = data;
			for (String methodName : topic.getAttributePath()) {
				Method m = currObj.getClass().getMethod(methodName);
				currObj = m.invoke(currObj);
			}
			res = currObj;
		} catch (Exception e) {
			Log.e("RosConnection", "parseAttribute failed!", e);
		}
		return res;
	}
	
	public static class Topic {
		String topic; 				//e.g. /hanse/position/estimate
		String rootType; 				//e.g. geometry_msgs/PoseStamped
		String type; 				//e.g. double (/ is replaced by .)
		List<String> attributePath; 	//e.g. [getPose,getPosition,getX]
		List<Topic> subTopics; 		//e.g. 
		
		public Topic(String topic, String type) {
			super();
			this.topic = topic;
			this.type = type;
			this.attributePath = new LinkedList<String>();
			this.subTopics = new LinkedList<Topic>();
		}
		
		public String getTopicName() {
			return topic;
		}
		public String getType() {
			return type;
		}
		public List<String> getAttributePath() {
			return attributePath;
		}
		public List<Topic> getSubTopics() {
			return subTopics;
		}
		public void setAttributPath(List<String> attributPath) {
			this.attributePath = attributPath;
		}
		public void setSubTopics(List<Topic> subTopics) {
			this.subTopics = subTopics;
		}
		public void setRootType(String rootType) {
			this.rootType = rootType;
		}
		public String getRootType() {
			return rootType;
		}

		public String getFullTopicName() {
			if (!attributePath.isEmpty()) {
				String attrPath = Joiner.on(".").join(Lists.transform(attributePath, new Function<String, String>() {
					@Override
					@Nullable
					public String apply(@Nullable String in) {
						if (in.startsWith("get")) {
							return in.substring(3);
						} 
						return in;
					}
				}));
				return topic + "@" + attrPath;
			}
			return topic;
		}
	}
	
	public static interface TopicsResultCallback {
		//TODO add error callback?
		public void resultCallback(List<Topic> topics);
	}
	
	public static interface RosDataConnection {
		public void setUpdateListener(RosDataUpdateListener updateListener);
		public void removeUpdateListener();
		public void shutdown();
	}
	
	public static interface RosDataUpdateListener {
		public void update(Object object);
	}
	
	public static class RegistrationException extends Exception {
		public RegistrationException(String s) {
			super(s);
		}
	}
}
 