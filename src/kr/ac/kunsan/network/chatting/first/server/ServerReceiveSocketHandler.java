package kr.ac.kunsan.network.chatting.first.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting.NetworkUtils;

public class ServerReceiveSocketHandler extends Thread {
	ChattingServer server;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private String nickName = "";

	ServerReceiveSocketHandler(Socket socket, ChattingServer server) throws IOException {
		this.socket = socket;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			// 서버에서 클라이언트와 통신할 소켓의 input/output Stream을 열어준다
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.inputStream = new ObjectInputStream(socket.getInputStream());

			// 소켓이 종료 되기전까지 계속 요청을 읽어온다
			while (!socket.isClosed()) {
				ChattingRequest chattingRequest = (ChattingRequest)inputStream.readObject();
				ChattingResponse response = new ChattingResponse();
				response.setMessageType(chattingRequest.getMessageType());
				String nickName = chattingRequest.getKey();
				ChattingRequest.MessageType type = chattingRequest.getMessageType();

				if (type == ChattingRequest.MessageType.JOIN) {
					if (server.registClient(nickName, outputStream)) {
						response.setSuccess(true);
						response.setNickName(nickName);
						response.setMessage(nickName + "님이 입장 하였습니다.");
						this.nickName = nickName;
						server.sendClients(response);
					} else {
						response.setMessage(nickName + "은 이미 등록되어 있는 닉네임 입니다. 닉네임을 다시 입력해 주세요");
						response.setSuccess(false);
						NetworkUtils.writeAndFlush(outputStream, response);
						this.nickName = "";
					}
				} else if(type == ChattingRequest.MessageType.MESSAGE) {
					if (nickName != null) {
						response.setSuccess(true);
						response.setNickName(nickName);
						response.setMessage(chattingRequest.getMessage());
						System.out.println(nickName + ": " + chattingRequest.getMessage());
						server.sendClients(response);
					} else {
						response.setMessage("닉네임을 먼저 등록해야 합니다.");
						response.setSuccess(false);
						NetworkUtils.writeAndFlush(outputStream, response);
					}
				} else if (type == ChattingRequest.MessageType.LEAVE) {
					response.setSuccess(true);
					response.setNickName(nickName);
					response.setMessage("방을 나가셨습니다.");
					server.removeClient(nickName);
					server.sendClients(response);
					break;
				}
			}
		} catch(Exception e) {
			ChattingResponse response = new ChattingResponse();
			response.setSuccess(true);
			response.setNickName(nickName);
			response.setMessage("오류로 인해 방을 퇴장 하였습니다.");
			server.removeClient(nickName);
			server.sendClients(response);
		} finally {
			server.removeClient(nickName);
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
