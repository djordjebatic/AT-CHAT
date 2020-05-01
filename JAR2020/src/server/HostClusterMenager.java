package server;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import models.Host;
import models.User;

@Singleton
@Startup
@Remote(ServerRest.class)
@Path("/hostCluster")
public class HostClusterMenager implements ServerRest{

	private String master = null;
	private Host hostNode = new Host();
	
	private List<Host> hostNodes = new ArrayList<Host>();
	
	
	@PostConstruct
	private void init() {
		try {
	        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");

			this.hostNode.setAddress((String) mBeanServer.getAttribute(http,"boundAddress") + ":8080");
			this.hostNode.setAlias(System.getProperty("jboss.node.name") + ":8080");

			this.master = "192.168.0.31:8080";
			
			System.out.println(" master: " + master + "\n host: " + this.hostNode.getAlias() + "\n host address: " + this.hostNode.getAddress());

			if (master != null && !master.equals("")) {
				System.out.println("MASTER...");
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rwTarget = client.target("http://" + master + "/WAR2020/rest/hostCluster");
				ServerRest rest = rwTarget.proxy(ServerRest.class);
				rest.registerHostNode(this.hostNode);
				
				System.out.println(this.hostNodes);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		

	}

	public List<Host> registerHostNode(Host host) {
		// TODO Auto-generated method stub
		ResteasyClient client = new ResteasyClientBuilder().build();
		for (Host node : this.hostNodes) {
			ResteasyWebTarget rwTarget = client.target("http://" + node.getAddress() + "/WAR2020/rest/hostCluster");
			ServerRest rest = rwTarget.proxy(ServerRest.class);
			rest.newHostNodeConnected(host);
		}

		this.hostNodes.add(host);
		return this.hostNodes;
	}

	public void newHostNodeConnected(Host host) {
		// TODO Auto-generated method stub
		this.hostNodes.add(host);
	}

	public void nonMasterHostNodesList(List<Host> hosts) {
		// TODO Auto-generated method stub
	}

	public void setLoggedIn(HashMap<String, User> loggedIn) {
		// TODO Auto-generated method stub
	}

	public HashMap<String, User> getLoggedIn() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void deleteHostNode(String alias) {
		// TODO Auto-generated method stub
	}
	
	public String checkHearthBeat() {
		// TODO Auto-generated method stub
		return null;
	}
}
