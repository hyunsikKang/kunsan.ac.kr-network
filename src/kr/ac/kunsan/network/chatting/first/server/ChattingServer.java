package kr.ac.kunsan.network.chatting.first.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting.NetworkUtils;

public class ChattingServer {
	private ServerSocket serverSocket;

	/**
	 * 클라이언트를 닉네임과 ObjectOutputStream을 Key/Value로 가지는 동시성 맵을 생성한다
	 */
	private Map<String, ObjectOutputStream> clientMap = new ConcurrentHashMap<>();

	public ChattingServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("포트를 전달하지 않았으므로 기본값인 PORT: 8080 으로 서버를 생성합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar ChattingServer 8080");
			args = new String[]{"8080"};
		}

		/**
		 * 채팅 서버를 생성한다
		 */
		ChattingServer server = new ChattingServer(Integer.valueOf(args[0]));
		System.out.println("채팅 서버 프로그램 입니다. 클라이언트를 기다리고 있습니다...");

		while (true) {
			/**
			 * 클라이언트를 기다린다.
			 * 소켓이 접속되면 신규 스레드를 생성하여 서버가 지속적으로 클라이언트의 접속을 받을 수 있도록 한다.
			 */
			Socket acceptSocket = server.serverSocket.accept();
			InetSocketAddress remoteSocketAddress = (InetSocketAddress)acceptSocket.getRemoteSocketAddress();
			System.out.println("클라이언트가 접속 하였습니다. IP : " + remoteSocketAddress.getAddress().getHostAddress() + ", PORT : " + acceptSocket.getPort());

			new ServerReceiveSocketHandler(acceptSocket, server).start();
		}
	}

	/**
	 * 닉네임으로 클라이언트를 등록한다.
	 * 동시에 등록되는 것을 방지하기 위해 synchronized 를 사용한다
	 * clientMap 자체는 ConcurrentHashMap 이지만 containsKey와 put이 하나의 동작이 아니기 때문에 synchronized 를 걸어준다
	 * @param nickName
	 * @param outputStream
	 * @return
	 */
	public boolean registClient(String nickName, ObjectOutputStream outputStream) {
		synchronized (clientMap) {
			if (clientMap.containsKey(nickName)) {
				return false;
			} else {
				clientMap.put(nickName, outputStream);
				return true;
			}
		}
	}

	/**
	 * 클라이언트를 닉네임으로 제거한다
	 * @param nickName
	 * @return
	 */
	public boolean removeClient(String nickName) {
		ObjectOutputStream remove = clientMap.remove(nickName);
		if (remove != null) {
			System.out.println(nickName + " 유저가 제거되었습니다.");
		}
		return remove != null;
	}

	/**
	 * 등록된 모든 클라이언트에게 채팅 메시지를 보낸다
	 * @param response
	 */
	public void sendClients(ChattingResponse response) {
		for (Map.Entry<String, ObjectOutputStream> entry : clientMap.entrySet()) {
			try {
				ObjectOutputStream outputStream = entry.getValue();
				NetworkUtils.writeAndFlush(outputStream, response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
