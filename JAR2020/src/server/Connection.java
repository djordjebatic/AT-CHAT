package server;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import beans.DataBean;
import models.Host;
import util.FileUtils;

@Singleton
@Startup
public class Connection{

	@EJB
	DataBean dataBean;
	
	private String master = null;

	private Host host = new Host();
	
	private List<Host> hostNodes = new ArrayList<Host>();
	
	
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

			if (master == null && !master.equals("")) {
				System.out.println("MASTER...");
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rwTarget = client.target("http://" + master + "/WAR2020/rest/connection");
				ConnectionManager rest = rwTarget.proxy(ConnectionManager.class);
				this.hostNodes = rest.registerHostNode(this.host);
				System.out.println("[Registration successful]");

				System.out.println(this.hostNodes);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @Schedule(hour = "*", minute = "*/1",second = "0")
    public void checkHeartBeat() {
    	System.out.println("Started hearthbeat");
    	ResteasyClient client = new ResteasyClientBuilder()
                .build();
    	for (Host node : this.hostNodes) {
    		ResteasyWebTarget rtarget = client.target("http://" + node.getAddress() + "/WAR2020/rest/connection");
    		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
    		Host host = rest.getNode();
    		if(host == null) {
    			Host host1 = rest.getNode();
    			if (host1 == null) {
    				removeNodeAndInformOthers(node);
    			}
    		}
    		
		}
    }

	private void removeNodeAndInformOthers(Host node) {
		ResteasyClient client = new ResteasyClientBuilder().build();;
		for (Host host : this.hostNodes) {
			if (!node.getAlias().equals(host.getAlias())) {
				ResteasyWebTarget rtarget = client.target("http://" + host.getAddress() + "/WAR2020/rest/connection");
	    		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
	    		rest.deleteHostNode(node.getAlias());
			}
		}
		this.hostNodes.remove(node);
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

	public void setHostNodes(List<Host> hostNodes) {
		this.hostNodes = hostNodes;
	}
    
    
}
