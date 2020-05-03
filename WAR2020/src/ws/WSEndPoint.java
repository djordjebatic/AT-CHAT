package ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@Singleton
@ServerEndpoint("/ws/{username}")
@LocalBean
public class WSEndPoint {
	public static Map<String, Session> sessions = new HashMap<>();
	
	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) throws IOException {
		if (!sessions.containsValue(session)) {
			sessions.put(username, session);
		}
	}
	
	@OnMessage
	public void echoTextMessage(Session receiver, String msg) throws IOException {
		try {
			if (receiver == null) {
				for (Map.Entry<String, Session> entry : sessions.entrySet()) {
	        		entry.getValue().getBasicRemote().sendText(msg);
				}
			} else {
				receiver.getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void updateLoggedInUsers(String users) {
    	for (Session session: sessions.values()) {
    		try {
				session.getBasicRemote().sendText(users);
				System.out.println("Logged in users: " + users);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
	@OnClose
	public void close(Session session, @PathParam("username")String username) throws IOException{
		sessions.remove(username);
		session.close();
	}
	
	@OnError
	public void error(Session session, Throwable t, @PathParam("username")String username) throws IOException {
		sessions.remove(username);
		session.close();
		t.printStackTrace();
	}

}
