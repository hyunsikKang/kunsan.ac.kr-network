package kr.ac.kunsan.network.chatting._3.nio.blocking.client;

import static kr.ac.kunsan.network.chatting._3.nio.SocketRequestResponseUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.NetworkUtils;

public class ClientHandler extends Thread {
	ByteBuffer buffer = ByteBuffer.allocate(1024 * 4);
	private SocketChannel socket;
	private BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
	private String nickName;

	/**
	 * 클라이언트의 행위를 제어하는 핸들러 생성자로
	 * 최초 서버에 유저의 조인을 알리기 위해 대화명을 입력 받는다.
	 * 입력 받은 후에는 서버에 채팅방 참여 메시지를 보내 대화명 등록 성공 여부를 전달 받는다.
	 * 만약 성공하지 못하였다면 성공할 때까지 대화명을 입력 받는다.
	 * @param socket
	 * @throws IOException
	 */
	public ClientHandler(SocketChannel socket) throws IOException {
		this.socket = socket;

		try {
			/**
			 * 모든 Request/Response는 ChattingRequest/ChattingResponse를 전달/회신 받는다
			 * Object를 Serialization/Deserialization 하기 위해 String으로 JSON 을 사용하여 serialize/deserialize를 한다.
			 */

			ChattingResponse response;
			do {
				/**
				 * 입장 메시지를 생성한다
				 * 사용자의 키보드로부터 대화명을 입력 받는다.
				 */
				ChattingRequest request = new ChattingRequest();
				request.setMessageType(ChattingRequest.MessageType.JOIN);
				request.setKey(keyboard.readLine());

				writeRequest(request, socket);

				/**
				 * 서버에게서 응답을 받는다.
				 * 성공일 경우 중복 없는 대화명이 등록 되고
				 * 키보드 입력을 받게 된다
				 */
				response = getChattingResponse(socket, buffer);

				if (response.isSuccess()) {
					nickName = request.getKey();
				} else {
					System.out.println(response.getMessage());
				}

			} while (!response.isSuccess());
			System.out.println(nickName + " 대화명이 채팅 서버에 등록 되었습니다.");

		} catch (Exception e) {
			// 예외 발생시 키보드 입력과 열었던 InputStream, OutputStream, 그리고 소켓을 닫는다
			// 자원을 정리하지 않을 경우 메모리의 누수가 발생할 수 있다.
			closeAllOfCloseableResources();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		startKeyboardReadAndSendThread();
		startReadFromServerThread();
	}

	/**
	 * 채팅 서버로부터 오는 메시지를 읽고 출력하는 스레드를 실행한다.
	 * 키보드 입력과 읽기는 별도 스레드로 분리되어야 정상적인 채팅 프로그램을 사용할 수 있다.
	 */
	private void startReadFromServerThread() {
		new Thread(new Runnable() {
			@Override public void run() {
				while (socket.isConnected()) {
					try {
						/**
						 * 서버에게서 응답을 받는다.
						 * 성공일 경우 중복 없는 대화명이 등록 되고
						 * 키보드 입력을 받게 된다
						 */
						ChattingResponse response = getChattingResponse(socket, buffer);
						System.out.println(response.getNickName() + ": " + response.getMessage());
					} catch (Exception e) {
						// 예외 발생시 키보드 입력과 열었던 InputStream, OutputStream, 그리고 소켓을 닫는다
						// 자원을 정리하지 않을 경우 메모리의 누수가 발생할 수 있다.
						closeAllOfCloseableResources();
						break;
					}
				}
			}
		}).start();
	}

	/**
	 * 사용자로부터 키보드 입력을 받아 채팅 서버로 전달하는 스레드를 실행한다
	 */
	private void startKeyboardReadAndSendThread() {
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					String input;
					while (socket.isConnected()) {
						/**
						 * 키보드 입력을 사용자에게서 !q를 입력하기 전까지 계속 입력 받는다
						 */
						input = keyboard.readLine();
						if ("!q".equals(input)) {
							/**
							 * 소켓이 접속 되어 있다면 퇴장 메시지를 보낸다
							 */
							if (socket.isConnected()) {
								ChattingRequest request = new ChattingRequest();
								request.setMessageType(ChattingRequest.MessageType.LEAVE);
								request.setKey(nickName);
								writeRequest(request, socket);
							}
							break;
						}
						try {

							/**
							 * 입력 받는 메시지를 서버에 닉네임과 함께 보낸다
							 */
							ChattingRequest request = new ChattingRequest();
							request.setMessageType(ChattingRequest.MessageType.MESSAGE);
							request.setMessage(input);
							request.setKey(nickName);

							writeRequest(request, socket);
						} catch (IOException e) {
							e.printStackTrace();
							closeAllOfCloseableResources();
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// 예외 발생시 키보드 입력과 열었던 소켓을 닫는다
					// 자원을 정리하지 않을 경우 메모리의 누수가 발생할 수 있다.
					closeAllOfCloseableResources();
				}
			}
		}).start();
	}

	/**
	 * 사용된 모든 자원을 정리한다
	 */
	private void closeAllOfCloseableResources() {
		NetworkUtils.closeQuietly(keyboard);
		NetworkUtils.closeQuietly(socket);
	}
}
