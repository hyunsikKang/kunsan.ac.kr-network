package kr.ac.kunsan.network.chatting;

import java.io.Serializable;

public class ChattingRequest implements Serializable {
	private static final long serialVersionUID = -2981871031726584516L;
	private String key;
	private String message;
	private MessageType messageType;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public enum MessageType {
		JOIN,
		LEAVE,
		MESSAGE
	}
}
