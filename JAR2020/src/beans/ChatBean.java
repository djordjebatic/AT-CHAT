package beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Chat;
import models.Host;
import models.Message;
import models.User;
import server.Connection;
import server.ConnectionManager;
import ws.WSEndPoint;


@Stateless
@Path("/chat")
@LocalBean
public class ChatBean {
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	
	@EJB
	WSEndPoint ws;
	
	//@EJB
	//ConnectionManager connectionManager;
	
	@EJB
	DataBean dataBean;
	
	@EJB
	Connection connection;
	
	//private Map<String, User> registeredUsers = new HashMap<String, User>();
	//private Map<String, User> loggedInUsers = new HashMap<String, User>();
	//private Map<String, Chat> chats = new HashMap<String, Chat>();
	
	@GET
	@Path("/users/exists/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public String checkExists(@PathParam("username") String username) {
		if (dataBean.getRegisteredUsers().get(username) != null) {
			return "yes";
		}
		else {
			return "no";
		}
	}
	
	@GET
	@Path("/chats/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserChats(@PathParam("username") String username){
		List<Chat> userChats = new ArrayList<>();
		for (Map.Entry<String, Chat> chat : dataBean.getChats().entrySet()) {
			for (String participant : chat.getKey().split(":")) {
				if (username.equals(participant)) {
					userChats.add(chat.getValue());
				}
			}
		}
		
		if (userChats.size() != 0) {
			return Response.status(Response.Status.OK).entity(userChats).build();
		}
		else {
			return Response.status(Response.Status.NO_CONTENT).entity(userChats).build();
		}
	}
	
	@POST
	@Path("/users/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response register(User user) {
		
		Host host = new Host(connection.getHost().getAlias(), connection.getHost().getAddress());
		System.out.println("Registered at adress: " + host.getAddress());
		user.setHost(host);
		
		for(User u : dataBean.getRegisteredUsers().values()) {
			System.out.println(user.getUsername());
			if(user.getUsername().equals(u.getUsername())) {
				return Response.status(409).entity("Username already exists").build();
			}
		}
		dataBean.getRegisteredUsers().put(user.getUsername(), user);
		
		
		for (Host h : Connection.hostNodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + h.getAddress() + "/WAR2020/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.setRegistered(dataBean.getRegisteredUsers());
			System.out.println("Added user on another host");
		}
		
		ObjectMapper mapper = new ObjectMapper();
        try {
			String jsonMessage = mapper.writeValueAsString(dataBean.getRegisteredUsers().values());
			ws.updateRegisteredUsers(jsonMessage);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        
		return Response.status(200).entity("Sucessful registration").build();
			
	}
	
	@POST
	@Path("/users/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response login(User user) {
		
		Host host = new Host(connection.getHost().getAlias(), connection.getHost().getAddress());
		System.out.println("Logged in at adress: " + host.getAddress());
		user.setHost(host);

		for(User u : dataBean.getRegisteredUsers().values()) {
			if(user.getUsername().equals(u.getUsername()) && u.getPassword().equals(user.getPassword())) {
				
				if (!user.isLoggedIn()) {
					user.setLoggedIn(true);
					dataBean.getLoggedInUsers().put(user.getUsername(), user);
					
					// TRIGGER UPDATE IN OTHER NODES
					for (Host h : Connection.hostNodes) {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget rwTarget = client.target("http://" + h.getAddress() + "/WAR2020/rest/connection");
						ConnectionManager rest = rwTarget.proxy(ConnectionManager.class);
						rest.setLoggedIn(dataBean.getLoggedInUsers());
						System.out.println("User added to another host");
					}
					
					
					ObjectMapper mapper = new ObjectMapper();
			        try {
						String jsonMessage = mapper.writeValueAsString(dataBean.getLoggedInUsers().values());
						ws.updateLoggedInUsers(jsonMessage);
					} catch (JsonProcessingException e) {
						System.out.println("Error while informing about login");
						e.printStackTrace();
					}
			        					
					return Response.status(200).entity("Sucessful login").build();
				}
			}
		}
		
		return Response.status(400).entity("Wrong username/password!").build();
	}
	
	@DELETE
	@Path("/users/loggedIn/{user}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response logout(@PathParam("user") String user) {
		
		for(User u : dataBean.getLoggedInUsers().values()) {
			if(u.getUsername().equals(user)) {
				dataBean.getLoggedInUsers().remove(u.getUsername());
				System.out.println("User " + user + " has signed out");
				
				ObjectMapper mapper = new ObjectMapper();
				
		        try {
					String jsonMessage = mapper.writeValueAsString(dataBean.getLoggedInUsers().values());
					ws.updateLoggedInUsers(jsonMessage);
				} catch (JsonProcessingException e) {
					System.out.println("Error while informing about login");
					e.printStackTrace();
				}
		        
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("User does not exist").build();
	}
	
	@GET
	@Path("/users/loggedIn")
	public void getLoggedUsers() {
		
		System.out.println("==========================================================");		
		System.out.println("LOGGED IN USERS");		
		int i = 0;
		for(User u : dataBean.getLoggedInUsers().values()) {
			System.out.println("User: " + i++ + ": " + u.getUsername());
		}
		System.out.println("==========================================================");		

	}
	
	@GET
	@Path("/users/registered")
	public void getRegisteredUsers() {
		
		System.out.println("==========================================================");
		System.out.println("REGISTERED USERS");		
		int i = 0;
		for(User u : dataBean.getRegisteredUsers().values()) {
			System.out.println("User " + i++ + ": " + u.getUsername());
		}
		System.out.println("==========================================================");
	}
	
	@POST
	@Path("/messages/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessageUser(Message message) {
		
		if (message == null || message.getSender() == null || message.getReceiver() == null || message.getText() == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid message").build();
		}
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			
			ObjectMessage objMessage = session.createObjectMessage(message);
			objMessage.setObject(message);
			sender.send(objMessage);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		sendMessageInChat(message);
		
		return Response.status(200).entity("Message has been sent").build();
	}
	
	@POST
	@Path("/messages/allUsers")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessageAllUsers(Message message) {
		if (message == null || message.getSender() == null || message.getText() == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid message").build();
		}
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			
			ObjectMessage objMessage = session.createObjectMessage(message);
			objMessage.setObject(message);
			sender.send(objMessage);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		sendMessageInChatToAll(message);
		
		return Response.status(200).entity("Messages have been sent").build();
	}
	
	public void sendMessageInChat(Message message) {
		LocalDateTime timestamp = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm dd.MM.yyyy.");
		String text = timestamp.format(formatter);
		message.setDate(text);
		System.out.println("Message to be sent from " + message.getSender() + " to " + message.getReceiver() + ". Text: " + message.getText() + ". DateTime: " + timestamp);
		
		String key = sortAlphabetical(message.sender, message.receiver);
		
		if (dataBean.getChats().get(key) == null) {
			Chat newChat = new Chat();
			ArrayList<String> participants = new ArrayList<>();
			participants.add(message.sender);
			participants.add(message.receiver);
			newChat.setParticipants(participants);
			ArrayList<Message> messages = new ArrayList<>();
			messages.add(message);
			newChat.setMessages(messages);
			dataBean.getChats().put(key, newChat);
		}
		else {
			Chat chat = dataBean.getChats().get(key);
			chat.getMessages().add(message);
		}
	}
	
	public void sendMessageInChatToAll(Message message) {
		LocalDateTime timestamp = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm dd.MM.yyyy.");
		String text = timestamp.format(formatter);
		message.setDate(text);
		System.out.println("Message to be sent from " + message.getSender() + " to everyone. Text: " + message.getText() + ". DateTime: " + timestamp);
		
		for (User user : dataBean.getRegisteredUsers().values()) {
			if (!message.getSender().equals(user.getUsername())) {
				String key = sortAlphabetical(message.getSender(), user.getUsername());
				message.setReceiver(user.getUsername());
				if (dataBean.getChats().get(key) == null) {
					Chat newChat = new Chat();
					ArrayList<String> participants = new ArrayList<>();
					participants.add(message.sender);
					participants.add(user.getUsername());
					newChat.setParticipants(participants);
					ArrayList<Message> messages = new ArrayList<>();
					messages.add(message);
					newChat.setMessages(messages);
					dataBean.getChats().put(key, newChat);
				}
				else {
					dataBean.getChats().get(key).getMessages().add(message);
				}
			}
		}
	}
	
	public String sortAlphabetical(String sender, String receiver) {
		int order = sender.compareTo(receiver);
		StringBuilder strBuilder = new StringBuilder();
		if (order >= 0) {
			strBuilder.append(receiver);
			strBuilder.append(":");
			strBuilder.append(sender);
		}
		else {
			strBuilder.append(sender);
			strBuilder.append(":");
			strBuilder.append(receiver);
		}
		return strBuilder.toString();
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	public Map<String, User> getLoggedInUsers() {
		return dataBean.getLoggedInUsers();
	}

	public void setLoggedInUsers(Map<String, User> loggedInUsers) {
		dataBean.setLoggedInUsers(loggedInUsers);
	}

	public Map<String, Chat> getChats() {
		return dataBean.getChats();
	}

	public void setChats(Map<String, Chat> chats) {
		dataBean.setChats(chats);;
	}

	public void setRegisteredUsers(Map<String, User> registeredUsers) {
		dataBean.setRegisteredUsers(registeredUsers);
	}
}