package kr.ac.kunsan.network.chatting._4.nio.async.future.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting._4.nio.async.future.FutureAsynchronousSocketRequestResponseUtils;

public class ServerReceiveSocketHandler implements Runnable {
	ChattingServer server;
	private AsynchronousSocketChannel socket;
	private String nickName = "";

	ServerReceiveSocketHandler(AsynchronousSocketChannel socket, ChattingServer server) throws IOException {
		this.socket = socket;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 1024);
			// 소켓이 종료 되기전까지 계속 요청을 읽어온다
			while (socket.isOpen()) {
				ChattingRequest chattingRequest = FutureAsynchronousSocketRequestResponseUtils.getChattingRequest(socket, buffer);
				ChattingResponse response = new ChattingResponse();
				response.setMessageType(chattingRequest.getMessageType());
				String nickName = chattingRequest.getKey();
				ChattingRequest.MessageType type = chattingRequest.getMessageType();

				if (type == ChattingRequest.MessageType.JOIN) {
					if (server.registClient(nickName, socket)) {
						response.setSuccess(true);
						response.setNickName(nickName);
						response.setMessage(nickName + "님이 입장 하였습니다.");
						this.nickName = nickName;
						server.sendClients(response);
					} else {
						response.setMessage(nickName + "은 이미 등록되어 있는 닉네임 입니다. 닉네임을 다시 입력해 주세요");
						response.setSuccess(false);
						FutureAsynchronousSocketRequestResponseUtils.writeResponse(response, socket);
						this.nickName = "";
					}
				} else if (type == ChattingRequest.MessageType.MESSAGE) {
					if (nickName != null) {
						response.setSuccess(true);
						response.setNickName(nickName);
						response.setMessage(chattingRequest.getMessage());
						System.out.println(nickName + ": " + chattingRequest.getMessage());
						server.sendClients(response);
					} else {
						response.setMessage("닉네임을 먼저 등록해야 합니다.");
						response.setSuccess(false);
						FutureAsynchronousSocketRequestResponseUtils.writeResponse(response, socket);
					}
				} else if (type == ChattingRequest.MessageType.LEAVE) {
					response.setSuccess(true);
					response.setNickName(nickName);
					response.setMessage("방을 나가셨습니다.");
					server.removeClient(socket);
					server.sendClients(response);
					break;
				}
			}
		} catch (Exception e) {
			ChattingResponse response = new ChattingResponse();
			response.setSuccess(true);
			response.setNickName(nickName);
			response.setMessage("오류로 인해 방을 퇴장 하였습니다.");
			server.removeClient(socket);
			server.sendClients(response);
		} finally {
			server.removeClient(socket);
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
