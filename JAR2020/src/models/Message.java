package models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable{

	private static final long serialVersionUID = 1L;

	public String text;
	public String sender;
	public String receiver;
	public String date;
	
	public Message() {
		
	}
	
	public Message(String text, String sender, String receiver, String date) {
		super();
		this.text = text;
		this.sender = sender;
		this.receiver = receiver;
		this.date = date;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
}
