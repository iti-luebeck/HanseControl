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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.ros.internal.message.Message;

import android.util.Log;

import geometry_msgs.PoseStamped;

public class RosMessageDataExtractorTest {
	
	static Set<String> nonRosMethods;
	
	static {
		nonRosMethods = new HashSet<String>();
		nonRosMethods.add("getClass");
		nonRosMethods.add("getHeader");
		nonRosMethods.add("getInstance");
		nonRosMethods.add("getInvocationHandler");
		nonRosMethods.add("getProxyClass");
		
//		Log.e("topicdiscoveryTest", "t1");
//		new Thread() {
//			public void run() {
////				Thread.sleep(1000);
//				Log.e("topicdiscoveryTest", "t2");
//				try {
//					testTopicDiscovery();
//				} catch (Exception e) {
//					Log.e("topicdiscoveryTest", e.getMessage());
//				}
//			};
//		}.start();
		
		try {
			classLookupTest();
		} catch (Exception e) {
			Log.e("classlookupTest", e.getMessage());
		}
	}
	
	public static void testPose(Message msg) throws Exception {
		Class<?> cl = msg.getClass();
		Method[] methods = cl.getMethods();
//		List<Method> getters = new LinkedList<Method>();
		
		for (Method m : methods) {
//			Log.d("reflectionTest", "Method: " + m.getName());
			if (m.getName().startsWith("get") && !nonRosMethods.contains(m.getName())) {
//				getters.add(m);
				Object data = m.invoke(msg, new Object[0]);
				Log.d("reflectionTest", "InnerObject: " + m.getName() + " of type: " + data.getClass().getName());
				if (data instanceof Message) {
					testPose((Message)data);
				}
			}
		}
		
	}
	
	public static void testTopicDiscovery() throws Exception {
		Log.e("topicdiscoveryTest", "t3");
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcClientConfigImpl clientConfig = new XmlRpcClientConfigImpl();
		clientConfig.setServerURL(new URL("http://ROS:11311"));
		Object result = client.execute(clientConfig, "getPublishedTopics", 
				new String[]{"/hansecontroltopiclookup", ""});
		for (Object o : (Object[])((Object[])result)[2]) {
			Log.e("topicdiscoveryTest", "Topic: " + ((Object[])o)[0] + " Type: " + ((Object[])o)[1]);
		}
		Log.e("topicdiscoveryTest", "Done");
	}
	
	public static void classLookupTest() throws Exception {
		String rosType = "geometry_msgs/PoseStamped";
		Class<?> cl = Class.forName(rosType.replace("/", "."));
		Log.e("classlookupTest", cl.getName());
	}
	
//	private static 
}



