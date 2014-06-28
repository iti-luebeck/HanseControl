package de.uniluebeck.iti.hanse.hansecontrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.ros.node.ConnectedNode;

import android.util.Log;

import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer.BehavioursListener;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer.IBehaviours;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer.Target;

public class TcpTestServerBehaviours implements IBehaviours {

	Set<String> behaviours = new HashSet<String>();
	static Server server;
	
	public TcpTestServerBehaviours() {
		if (server == null) {
			server = new Server();
			server.start();			
		}
		
		behaviours.add("Wall Follow");
		behaviours.add("Pipe Follow");
		behaviours.add("Circle");
	}
	
	@Override
	public Set<String> getBehaviours() {
		return behaviours;
	}

	@Override
	public void sendGoal(Target target) {
		server.writeToClients(String.format("sendGoal(%f, %f)", target.getRosPos().x, target.getRosPos().y));
	}

	@Override
	public void setBehavioursListener(BehavioursListener behavioursListener) {
		server.setBehavioursListener(behavioursListener);
	}

	@Override
	public void startBehaviour(String name) {
		server.writeToClients(String.format("startBehaviour(%s)", name));
	}

	@Override
	public void setNode(ConnectedNode node) { }
	
}

class Server extends Thread {
	
	ServerSocket serverSocket;
	List<PrintWriter> connectedWriter = new LinkedList<PrintWriter>();
	
	BehavioursListener behavioursListener;
	
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(8888);
			while(true) {
				final Socket client = serverSocket.accept();
				MainScreen.getExecutorService().execute(new Runnable() {
					@Override
					public void run() {
						try {
							handleClient(client);
						} catch (Exception e) {
							Log.e("TcpTestServer", "HandleClient exception", e);
						}
					}
				});
			}
		} catch (Exception e) {
			Log.e("TcpTestServer", "Server exception", e);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {}
		}
	}
	
	public void handleClient(Socket client) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		PrintWriter pw = new PrintWriter(client.getOutputStream());
		connectedWriter.add(pw);
		for (String line; (line = br.readLine()) != null;) {
			if (line.equals("test")) {
				pw.println("ok!");
			} else if (line.equals("shutdown")) {
				pw.println("Closing server...");
				client.close();
				serverSocket.close();
			} else if (line.equals("close")) {
				pw.println("Closing socket...");
				client.close();
			} else if (line.equals("goalreached")) {
				pw.println("calling goalreached()...");
				behavioursListener.goalReached();
			} else if (line.startsWith("finished ")) {
				String name = line.substring("finished ".length());
				pw.println("calling behaviourFinished("+name+")...");
				behavioursListener.behaviourFinished(name);
			} else {
				pw.println("Commands: shutdown, close, test, goalreached, finished [behaviourname]");
			}
			pw.flush();
		}
	}
	
	public void writeToClients(String line) {
		for (PrintWriter pw : connectedWriter) {
			try {
				pw.println(line);
				pw.flush();				
			} catch (Exception e) {
			}
		}
	}
	
	public void setBehavioursListener(BehavioursListener behavioursListener) {
		this.behavioursListener = behavioursListener;
	}
}
