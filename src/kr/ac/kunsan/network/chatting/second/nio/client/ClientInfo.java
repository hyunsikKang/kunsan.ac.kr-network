package kr.ac.kunsan.network.chatting.second.nio.client;

import java.io.ObjectOutputStream;

public class ClientInfo {
	private String nickName;
	private ObjectOutputStream outputStream;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
	}
}
