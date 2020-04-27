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

import models.Chat;
import models.Message;
import models.User;
import ws.WSEndPoint;


@Stateless
@Path("/chat")
@LocalBean
public class ChatBean implements ChatRemote, ChatLocal {
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	
	@EJB
	WSEndPoint ws;
	
	private Map<String, User> registeredUsers = new HashMap<String, User>();
	private Map<String, User> loggedInUsers = new HashMap<String, User>();
	private Map<String, Chat> chats = new HashMap<String, Chat>();
	
	@GET
	@Path("/users/exists/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public String checkExists(@PathParam("username") String username) {
		if (registeredUsers.get(username) != null) {
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
		for (Map.Entry<String, Chat> chat : chats.entrySet()) {
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
		
		for(User u : registeredUsers.values()) {
			if(user.getUsername().equals(u.getUsername())) {
				return Response.status(409).entity("Username already exists").build();
			}
		}
		this.registeredUsers.put(user.getUsername(), user);
		return Response.status(200).build();
			
	}
	
	@POST
	@Path("/users/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response login(User user) {
				
		for(User u : registeredUsers.values()) {
			if(user.getUsername().equals(u.getUsername()) && u.getPassword().equals(user.getPassword())) {
				
				if (!user.isLoggedIn()) {
					user.setLoggedIn(true);
					this.loggedInUsers.put(user.getUsername(), user);
				}
				return Response.status(200).build();
			}
		}
		
		return Response.status(400).entity("Wrong username/password!").build();
	}
	
	@DELETE
	@Path("/users/loggedIn/{user}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response logout(@PathParam("user") String user) {
		
		for(User u : loggedInUsers.values()) {
			if(u.getUsername().equals(user)) {
				loggedInUsers.remove(u.getUsername());
				System.out.println("User " + user + " has signed out");
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
		for(User u : loggedInUsers.values()) {
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
		for(User u : registeredUsers.values()) {
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
		
		if (chats.get(key) == null) {
			Chat newChat = new Chat();
			ArrayList<String> participants = new ArrayList<>();
			participants.add(message.sender);
			participants.add(message.receiver);
			newChat.setParticipants(participants);
			ArrayList<Message> messages = new ArrayList<>();
			messages.add(message);
			newChat.setMessages(messages);
			chats.put(key, newChat);
		}
		else {
			Chat chat = chats.get(key);
			chat.getMessages().add(message);
		}
	}
	
	public void sendMessageInChatToAll(Message message) {
		LocalDateTime timestamp = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm dd.MM.yyyy.");
		String text = timestamp.format(formatter);
		message.setDate(text);
		System.out.println("Message to be sent from " + message.getSender() + " to everyone. Text: " + message.getText() + ". DateTime: " + timestamp);
		
		for (User user : registeredUsers.values()) {
			if (!message.getSender().equals(user.getUsername())) {
				String key = sortAlphabetical(message.getSender(), user.getUsername());
				message.setReceiver(user.getUsername());
				if (chats.get(key) == null) {
					Chat newChat = new Chat();
					ArrayList<String> participants = new ArrayList<>();
					participants.add(message.sender);
					participants.add(user.getUsername());
					newChat.setParticipants(participants);
					ArrayList<Message> messages = new ArrayList<>();
					messages.add(message);
					newChat.setMessages(messages);
					chats.put(key, newChat);
				}
				else {
					chats.get(key).getMessages().add(message);
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
	
	@Override
	public String test() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String post(String text) {
		// TODO Auto-generated method stub
		return null;
	}
}
