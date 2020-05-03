package beans;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import models.Chat;
import models.User;

@Singleton
@LocalBean
@Startup
public class DataBean {

	private Map<String, User> registeredUsers = new HashMap<String, User>();
	private Map<String, User> loggedInUsers = new HashMap<String, User>();
	private Map<String, Chat> chats = new HashMap<String, Chat>();
	
	public DataBean() {
		
	}
	
	public Map<String, User> getRegisteredUsers() {
		return registeredUsers;
	}
	public void setRegisteredUsers(Map<String, User> registeredUsers) {
		this.registeredUsers = registeredUsers;
	}
	public Map<String, User> getLoggedInUsers() {
		return loggedInUsers;
	}
	public void setLoggedInUsers(Map<String, User> loggedInUsers) {
		this.loggedInUsers = loggedInUsers;
	}
	public Map<String, Chat> getChats() {
		return chats;
	}
	public void setChats(Map<String, Chat> chats) {
		this.chats = chats;
	}
}
