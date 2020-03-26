package tileworld.agent;

public class Message {
	private String from; // the sender
	private String to; // the recepient
	private String message; // the message
	
	public Message(String from, String to, String message){
		this.from = from;
		this.to = to;
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getMessage() {
		return message;
	}

}
