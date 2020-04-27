package ws;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.websocket.Session;

import com.fasterxml.jackson.databind.ObjectMapper;




@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})
public class QueueMDB implements MessageListener {
	@EJB 
	WSEndPoint ws;
	
	@Override
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
	}

}
