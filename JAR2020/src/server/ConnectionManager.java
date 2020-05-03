package server;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import models.Host;
import models.User;

//@Local
public interface ConnectionManager {

	@POST
	@Path("/register")
	@Consumes(value = MediaType.APPLICATION_JSON)
	public Response registerHostNode(Host host);
	
	@POST
	@Path("/node")
	@Consumes(value = MediaType.APPLICATION_JSON)
	public void addHostNode(Host host);
	
	@POST
	@Path("/nodes")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllHostNodes(List<Host> hosts);
	
	@POST
	@Path("/users/loggedIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setLoggedIn(Map<String, User> loggedInUsers);
	
	@GET
	@Path("/users/loggedIn")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, User> getLoggedIn();

	@DELETE
	@Path("/node/{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean deleteHostNode(@PathParam("alias") String alias);
	
	@GET
	@Path("/node")
	@Produces(MediaType.APPLICATION_JSON)
	public Host getNode();
	
	@GET
	@Path("/users/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, User> getRegistered();
	
	@POST
	@Path("/users/set-registered")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setRegistered(Map<String, User> registered);
}