package server;

import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import beans.DataBean;
import models.Host;
import models.User;
import ws.WSEndPoint;

@Stateless
@LocalBean
@Path("/connection")
@Remote(ConnectionManager.class)
public class ConnectionManagerBean implements ConnectionManager {

	@EJB
	DataBean dataBean;
	
	@EJB
	WSEndPoint ws;
	
	@EJB
	Connection connection;
	
    public ConnectionManagerBean() {
    }
    
	@Override
	public List<Host> registerHostNode(Host host) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		for (Host node : connection.getHostNodes()) {
    		ResteasyWebTarget rtarget = client.target("http://" + node.getAddress() + "/WAR2020/rest/connection");
    		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
    		rest.getHostNode(host);	
		}

    	ResteasyWebTarget rtarget = client.target("http://" + host.getAddress() + "/WAR2020/rest/connection");
    	connection.getHostNodes().add(host);
    	return connection.getHostNodes();
	}

	@Override
	public Host getHostNode(Host host) {
		for (Host node : connection.getHostNodes()) {
			if (node.getAlias().equals(host.getAlias())) {
				return node;
			}
		}
		
		connection.getHostNodes().add(host);
		return host;
	}

	@Override
	public void getAllHostNodes(List<Host> hosts) {
		
		
	}

	@Override
	public void setLoggedIn(HashMap<String, User> loggedIn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, User> getLoggedIn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteHostNode(String alias) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Host getNode() {
		// TODO Auto-generated method stub
		return null;
	}	
}
