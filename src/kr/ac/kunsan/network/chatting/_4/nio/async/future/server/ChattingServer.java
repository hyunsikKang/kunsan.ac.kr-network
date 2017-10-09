package kr.ac.kunsan.network.chatting._4.nio.async.future.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting.JsonRequestResponseConverter;
import kr.ac.kunsan.network.chatting.NetworkUtils;

public class ChattingServer {
	/**
	 * 클라이언트를 SocketChannel과 nickname을 Key/Value로 가지는 맵을 생성한다. non blocking single thread 모델이므로 ConcurrentHashMap을 사용할 필요가 없다.
	 */
	private Map<SocketChannel, String> clientMap = new HashMap<>();
	private List<ChattingResponse> writeResponseList = new ArrayList<>();
	private ByteBuffer buffer = ByteBuffer.allocate(1024*4);

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


				/*try (AsynchronousSocketChannel acceptedSocket = serverSocket.accept().get()) {
					while(acceptedSocket.read(buffer).get() != -1) {

					}
				}*/
			}
		}

	}

	private void writeHandle(SelectionKey key) {
		ListIterator<ChattingResponse> iterator = writeResponseList.listIterator();
		while (iterator.hasNext()) {
			ChattingResponse response = iterator.next();
			iterator.remove();
			sendClients(response);
		}
		// write를 하였으므로 read 동작을 수행할 수 있도록 셀렉터의 관심 오퍼레이션을 리드로 변경한다
		key.interestOps(SelectionKey.OP_READ);
	}



	private void acceptHandle(SelectionKey key, Selector selector) throws IOException, ClassNotFoundException {
		ServerSocketChannel channel = (ServerSocketChannel)key.channel();
		SocketChannel acceptedClientSocketChannel = channel.accept();
		// non blocking 설정을 한다
		acceptedClientSocketChannel.configureBlocking(false);

		InetSocketAddress remoteSocketAddress = (InetSocketAddress)acceptedClientSocketChannel.getRemoteAddress();
		System.out.println("클라이언트가 접속 하였습니다. IP : " + remoteSocketAddress.getAddress().getHostAddress() + ", PORT : " + remoteSocketAddress.getPort());

		// 클라이언트 소켓을 셀렉터에 read로 등록한다
		// 클라이언트로 부터는 리드 동작만 등록한다
		acceptedClientSocketChannel.register(selector, SelectionKey.OP_READ);
	}

	/**
	 * 닉네임으로 클라이언트를 등록한다.
	 * non blocking 이므로 동시성을 생각할 필요 없이 synchronized 키워드 없이 클라이언트를 등록한다
	 * @param nickName
	 * @param socketchannel
	 * @return
	 */
	public boolean registClient(String nickName, SocketChannel socketchannel) {
		if (clientMap.containsValue(nickName)) {
			return false;
		} else {
			clientMap.put(socketchannel, nickName);
			return true;
		}
	}

	/**
	 * 클라이언트를 닉네임으로 제거한다
	 * @return
	 */
	public boolean removeClient(SelectionKey key) {
		SelectableChannel socketChannel = key.channel();
		String nickName = clientMap.remove(socketChannel);
		if (socketChannel != null && socketChannel.isOpen()) {
			NetworkUtils.closeQuietly(socketChannel);
			key.cancel();

			System.out.println(nickName + " 유저가 제거되었습니다.");
		}
		return socketChannel != null;
	}

	/**
	 * 등록된 모든 클라이언트에게 채팅 메시지를 보낸다
	 * @param response
	 */
	public void sendClients(ChattingResponse response) {
		for (SocketChannel socketChannel: clientMap.keySet()) {
			try {
//				JsonRequestResponseConverter.writeResponse(response, socketChannel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("포트를 전달하지 않았으므로 기본값인 PORT: 8080 으로 서버를 생성합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar ChattingServer 8080");
			args = new String[]{"8080"};
		}

		/**
		 * 채팅 서버를 생성한다
		 */
		new ChattingServer(Integer.valueOf(args[0]));
	}
}
