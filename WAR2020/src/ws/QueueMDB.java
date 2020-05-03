package ws;

import java.util.ArrayList;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.websocket.Session;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.databind.ObjectMapper;

import beans.DataBean;
import models.Chat;
import models.User;
import server.Connection;




@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})
public class QueueMDB implements MessageListener {
	@EJB 
	WSEndPoint ws;
	
	@EJB
	DataBean dataBean;
	
	@EJB
	Connection connection;
	
	public void onMessage(Message msg) {
		
		try {
			ObjectMessage omsg = (ObjectMessage) msg;
			models.Message message = (models.Message) omsg.getObject();

			System.out.println("RECEIVER: " + message.getReceiver());
			System.out.println("SENCEDR: " + message.getSender());

			String json;
			try {
				ObjectMapper mapper = new ObjectMapper();
				json = mapper.writeValueAsString(omsg.getObject());
				
				System.out.println(json);
				System.out.println(omsg.toString());
				//receiver = omsg.getStringProperty("receiver");
				//sender = omsg.getStringProperty("sender");
				
				Session sessionReciever = null;
				
				ResteasyClient client = new ResteasyClientBuilder().build();

				// Send everyone
				if (message.getReceiver() == null) {
					for (User user : dataBean.getRegisteredUsers().values()) {
						System.out.println( user.getUsername() + " konekcija = " + dataBean.getRegisteredUsers().get(user.getUsername()).getHost().getAddress());
						if (dataBean.getRegisteredUsers().get(user.getUsername()).getHost().getAddress()
								.equals(connection.getHost().getAddress())) {
							Session sessionSender = ws.sessions.get(message.getSender());
							ws.echoTextMessage(sessionSender, json);
						}
					}
				}
				
				else {
					for (User user : dataBean.getRegisteredUsers().values()) {
						if (user.getUsername().equals(message.getSender())) {
							System.out.println("SENDER SKIDIBA");
							Session sessionSender = ws.sessions.get(message.getSender());
							ws.echoTextMessage(sessionSender, json);
						}
						else if (user.getUsername().equals(message.getReceiver())) {
							if (user.getHost().getAddress().equals(connection.getHost().getAddress())) {
								System.out.println("RECEIVER ISTI HOST SKIDIBA");
								Session sessionSender = ws.sessions.get(message.getSender());
								ws.echoTextMessage(sessionSender, json);
							} else {
								String path = "http://"
										+ dataBean.getRegisteredUsers().get(user.getUsername()).getHost().getAddress()
										+ "/WAR2020/rest/chat/messages/user";
								ResteasyWebTarget rtarget = client.target(path);
								Response response = rtarget.request(MediaType.APPLICATION_JSON)
										.post(Entity.entity(message, MediaType.APPLICATION_JSON));
								System.out.println("Receiveru na drugi host, Response: " + response.getStatus());
							}
						}
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		}
		catch (Exception e) {
			
		}
	}
	
	/*@Override
	public void onMessage(Message msg) {
		ObjectMessage omsg = (ObjectMessage) msg;
		String reciever = "";
		String sender = "";
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(omsg.getObject());
			
			reciever = omsg.getStringProperty("reciever");
			sender = omsg.getStringProperty("sender");
			
			Session sessionReciever = null;
			if (reciever != null) {
				sessionReciever = ws.sessions.get(reciever);
				ws.echoTextMessage(sessionReciever, json);
			}
			Session sessionSender = ws.sessions.get(sender);
			ws.echoTextMessage(sessionSender, json);
		} catch (JMSException e) {
			e.printStackTrace();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}*/

}
