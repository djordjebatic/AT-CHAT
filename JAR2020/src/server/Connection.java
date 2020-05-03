package server;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import beans.ChatBean;
import models.Host;
import models.User;
import util.FileUtils;

@Singleton
@Startup
@Path("/connection")
public class Connection implements ConnectionManager{

	@EJB
	private ChatBean chatBean;
	
	private String master = null;

	private Host host = new Host();
	
	public static List<Host> hostNodes = new ArrayList<Host>();
	
	//SIEBOG
	@PostConstruct
	private void init() {
		try {
	        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");

            host.setAddress((String) mBeanServer.getAttribute(http,"boundAddress") + ":8080");
            host.setAlias(System.getProperty("jboss.node.name") + ":8080");

			File f = FileUtils.getFile(ConnectionManager.class, "", "connections.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			this.master = properties.getProperty("master");			
			System.out.println(" master: " + master + "\n host: " + this.host.getAddress() + "\n host address: " + this.host.getAlias());

			if (master != null && !master.equals("")) {
				System.out.println("[Slave host] ...");
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rwTarget = client.target("http://" + master + "/WAR2020/rest/connection");
				ConnectionManager rest = rwTarget.proxy(ConnectionManager.class);
				rest.registerHostNode(host);
				System.out.println("[REGISTERED NODES]" + hostNodes);
				chatBean.setLoggedInUsers(rest.getLoggedIn());
				
				System.out.println("[LOGGED IN]" + "-----");
				for (Map.Entry<String, User> u : chatBean.getLoggedInUsers().entrySet()) {
					System.out.println(u.getKey());
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @Schedule(hour = "*", minute = "*/1",second = "0")
    public void checkHeartBeat() {
    	System.out.println("Started hearthbeat");
    	ResteasyClient client = new ResteasyClientBuilder().build();
    	for (Host node : hostNodes) {
    		// CHECK IF NODE EXISTS
    		try{
    			ResteasyWebTarget rtarget = client.target("http://" + node.getAddress() + "/WAR2020/rest/connection");
    			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
    			Host host = rest.getNode();
	    		if(host == null) {
	    			try {
	    				// TRY CHECKING ONE MORE TIME
	    		    	ResteasyClient client2 = new ResteasyClientBuilder().build();
	    				ResteasyWebTarget rtarget2 = client2.target("http://" + node.getAddress() + "/WAR2020/rest/connection");
	        			ConnectionManager rest2 = rtarget2.proxy(ConnectionManager.class);
	        			Host host2 = rest2.getNode();
		    			if (host2 == null) {
		    				removeNodeAndInformOthers(node);
		    			}
	    			}
	    			catch (Exception e) {
	    				System.out.println("ERROR WHILE CHECKING HEARTHBEAT");
	        			e.printStackTrace();
					}
	    		}
    		}
    		catch (Exception e) {
				System.out.println("ERROR WHILE CHECKING HEARTHBEAT");
    			e.printStackTrace();
    		}
    		
		}
    }

	private void removeNodeAndInformOthers(Host node) {
		ResteasyClient client = new ResteasyClientBuilder().build();;
		for (Host host : hostNodes) {
			if (!node.getAlias().equals(host.getAlias())) {
				try{
					ResteasyWebTarget rtarget = client.target("http://" + host.getAddress() + "/WAR2020/rest/connection");
					ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
					rest.deleteHostNode(node.getAlias());
				}
				catch (Exception e) {
					System.out.println("ERROR WHILE DELETING NODE");
				}
			}
		}
		hostNodes.remove(node);
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public List<Host> getHostNodes() {
		return hostNodes;
	}

	public void setHostNodes(List<Host> nodes) {
		hostNodes = nodes;
	}

	@Override
	public Response registerHostNode(Host host) {
		
		// 1ST - ADD TO MASTER
		if (this.master != null) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + this.master + "/WAR2020/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.addHostNode(host);
		}
		
		// 2ND - OTHER NODES SHOULD GET FROM MASTER NODE
		for (Host node : hostNodes) {
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + node.getAddress() + "/WAR2020/connection");
				ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
				rest.addHostNode(host);
			}
			catch (Exception e) {
				System.out.println("ERROR WHILE ADDING A NEW NODE ");
			}
		}
		
		hostNodes.add(host);
		return Response.status(200).build();
	}

	@Override
	public void addHostNode(Host host) {
		hostNodes.add(host);
	}

	@Override
	public Response getAllHostNodes(List<Host> hosts) {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoggedIn(Map<String, User> loggedIn) {
		// TODO Auto-generated method stub
		chatBean.setLoggedInUsers(loggedIn);
	}

	@Override
	public Map<String, User> getLoggedIn() {
		return chatBean.getLoggedInUsers();
	}

	@Override
	public boolean deleteHostNode(String alias) {
		for (Host node : hostNodes) {
			if (node.getAlias().equals(alias)) {
				hostNodes.remove(node);
				System.out.println("Deleted node " + node);
				return true;
			}
		}
		return false;
	}

	@Override
	public Host getNode() {
		return this.host;
	}


	@Override
	public void setRegistered(Map<String, User> registeredUsers) {
		chatBean.setRegisteredUsers(registeredUsers);
	}
    
	@PreDestroy
	private void destroy() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		for (Host h : hostNodes) {
			try{
				ResteasyWebTarget rtarget = client.target("http://" + h.getAddress() + "/WAR2020/rest/connection");
				ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
				rest.deleteHostNode(this.host.getAlias());
			}
			catch (Exception e) {
				System.out.println("ERROR WHILE DESTROYING NODE");
				e.printStackTrace();
			}
		}
	}
}
