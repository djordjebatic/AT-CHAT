package ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import beans.ChatLocal;

@Singleton
@ServerEndpoint("/ws")
@LocalBean
public class WSEndPoint {
	static List<Session> sessions = new ArrayList<Session>();
	
	@EJB
	ChatLocal chat;
	
	@OnOpen
	public void onOpen(Session session) {
		if (!sessions.contains(session)) {
			sessions.add(session);
		}
	}
	
	@OnMessage
	public void echoTextMessage(String msg) {
		System.out.println("ChatBean returned: " + chat.test());
		
		try {
	        for (Session s : sessions) {
	        	System.out.println("WSEndPoint: " + msg);
        		s.getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void close(Session session) {
		sessions.remove(session);
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		sessions.remove(session);
		t.printStackTrace();
	}

}
