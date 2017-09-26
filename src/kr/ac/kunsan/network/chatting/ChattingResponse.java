package kr.ac.kunsan.network.chatting;

import java.io.Serializable;

public class ChattingResponse implements Serializable {
	private static final long serialVersionUID = -553385399705843431L;
	private String nickName;
	private String message;
	private boolean success;
	private ChattingRequest.MessageType messageType;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public ChattingRequest.MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(ChattingRequest.MessageType messageType) {
		this.messageType = messageType;
	}
}
