package kr.ac.kunsan.network.chatting._4.nio.async.future.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting.NetworkUtils;
import kr.ac.kunsan.network.chatting._4.nio.async.future.FutureAsynchronousSocketRequestResponseUtils;

public class ChattingServer {
	private Map<AsynchronousSocketChannel, String> clientMap = new ConcurrentHashMap<>();

	public ChattingServer(int port) throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newCachedThreadPool();
		try (AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open()) {

			if (!serverSocket.isOpen()) {
				throw new IllegalStateException("소켓과 셀렉터가 제대로 열리지 않았습니다.");
			}

			/**
			 * Socket 옵션을 조정한다
			 * SO_REUSEADDR : TIME_WAIT 상태의 소켓을 재사용 한다.
			 */
			serverSocket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			/**
			 * 수신 받을 버퍼의 사이즈를 지정한다
			 */
			serverSocket.setOption(StandardSocketOptions.SO_RCVBUF, 2 * 1024);
			serverSocket.bind(new InetSocketAddress("127.0.0.1", port));

			System.out.println("채팅 서버 프로그램 입니다. 클라이언트를 기다리고 있습니다...");

			while (true) {
				AsynchronousSocketChannel acceptedSocket = serverSocket.accept().get();
				InetSocketAddress remoteSocketAddress = (InetSocketAddress)acceptedSocket.getRemoteAddress();
				System.out.println("클라이언트가 접속 하였습니다. IP : " + remoteSocketAddress.getAddress().getHostAddress() + ", PORT : " + remoteSocketAddress.getPort());

				executorService.submit(new ServerReceiveSocketHandler(acceptedSocket, this));
			}
		}

	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("포트를 전달하지 않았으므로 기본값인 PORT: 8080 으로 서버를 생성합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar ChattingServer 8080");
			args = new String[] {"8080"};
		}

		/**
		 * 채팅 서버를 생성한다
		 */
		new ChattingServer(Integer.valueOf(args[0]));
	}

	/**
	 * 닉네임으로 클라이언트를 등록한다.
	 * 동시에 등록되는 것을 방지하기 위해 synchronized 를 사용한다
	 * clientMap 자체는 ConcurrentHashMap 이지만 containsKey와 put이 하나의 동작이 아니기 때문에 synchronized 를 걸어준다
	 * @param nickName
	 * @param socketChannel
	 * @return
	 */
	public boolean registClient(String nickName, AsynchronousSocketChannel socketChannel) {
		synchronized (clientMap) {
			if (clientMap.containsValue(nickName)) {
				return false;
			} else {
				clientMap.put(socketChannel, nickName);
				return true;
			}
		}
	}

	/**
	 * 클라이언트를 닉네임으로 제거한다
	 * @return
	 */
	public boolean removeClient(AsynchronousSocketChannel socketChannel) {
		String nickName = clientMap.remove(socketChannel);
		if (socketChannel != null && socketChannel.isOpen()) {
			NetworkUtils.closeQuietly(socketChannel);

			System.out.println(nickName + " 유저가 제거되었습니다.");
		}
		return socketChannel != null;
	}

	/**
	 * 등록된 모든 클라이언트에게 채팅 메시지를 보낸다
	 * @param response
	 */
	public void sendClients(ChattingResponse response) {
		for (AsynchronousSocketChannel socketChannel : clientMap.keySet()) {
			try {
				FutureAsynchronousSocketRequestResponseUtils.writeResponse(response, socketChannel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
