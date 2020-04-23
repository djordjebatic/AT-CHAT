package models;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable{

	private static final long serialVersionUID = 1L;

	public ArrayList<String> participants = new ArrayList<>();
	public boolean receiverHasRead = false;
	public ArrayList<Message> messages = new ArrayList<>();
	
	public Chat() {

	}
	
	public ArrayList<String> getParticipants() {
		return participants;
	}
	public void setParticipants(ArrayList<String> participants) {
		this.participants = participants;
	}
	public boolean isReceiverHasRead() {
		return receiverHasRead;
	}
	public void setReceiverHasRead(boolean receiverHasRead) {
		this.receiverHasRead = receiverHasRead;
	}
	public ArrayList<Message> getMessages() {
		return messages;
	}
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
}
