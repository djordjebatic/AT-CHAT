package server;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import models.Host;
import models.User;

public interface ConnectionManager {

	@POST
	@Path("/register")
	@Consumes(value = MediaType.APPLICATION_JSON)
	@Produces(value = MediaType.APPLICATION_JSON)
	public List<Host> registerHostNode(Host host);
	
	@POST
	@Path("/node")
	@Consumes(value = MediaType.APPLICATION_JSON)
	public void getHostNode(Host host);
	
	@POST
	@Path("/nodes")
	@Consumes(MediaType.APPLICATION_JSON)
	public void getAllHostNodes(List<Host> hosts);
	
	@POST
	@Path("/users/loggedIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setLoggedIn(HashMap<String, User> loggedIn);
	
	@GET
	@Path("/users/loggedIn")
	@Produces(MediaType.APPLICATION_JSON)
	public HashMap<String, User> getLoggedIn();

	@DELETE
	@Path("/node/{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteHostNode(@PathParam("alias") String alias);
	
	@GET
	@Path("/node")
	@Produces(MediaType.APPLICATION_JSON)
	public Host getNode();
	
	/* @PreDestroy
	private void destroy() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		for (Host h : hostNodes) {
			ResteasyWebTarget rtarget = client.target("http://" + h.getAddress() + "/ChatWAR/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.deleteHostNode(this.host.getAlias());
		}
		
	}

	@Override
	public List<Host> registerHostNode(Host host) {
		System.out.println("New node registered: " + host);
		for (Host node : hostNodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rwTarget = client.target("http://" + node + "/WAR2020/rest/connection");
			ConnectionManager rest = rwTarget.proxy(ConnectionManager.class);
			rest.newHostNodeConnection(host);
		}

		hostNodes.add(host);
		return hostNodes;
	}

	@Override
	public void newHostNodeConnection(Host host) {
		System.out.println("New host is here: " + host + ", address: " + host);
		this.hostNodes.add(host);
		System.out.println(this.hostNodes);
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
		for (Host h : hostNodes) {
			if (h.getAlias().equals(alias)) {
				hostNodes.remove(h);
			}
		}
	}
	
	@Override
	public String checkHearthBeat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void nonMasterHostNodesList(List<Host> hosts) {
		// TODO Auto-generated method stub
		
	}*/
}