package kr.ac.kunsan.network.chatting.third.nio.nonblocking.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
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

	public ChattingServer(int port) throws IOException, ClassNotFoundException {
		try (Selector selector = Selector.open();
			 ServerSocketChannel serverSocket = ServerSocketChannel.open();
		) {

			if (!serverSocket.isOpen() || !selector.isOpen()) {
				throw new IllegalStateException("소켓과 셀렉터가 제대로 열리지 않았습니다.");
			}

			// 서버를 Non-blocking 모드로 설정한다
			serverSocket.configureBlocking(false);

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

			// 서버 소켓은 accept에 대해서만 관심이 있기 때문에 셀렉터에 ACCEPT 를 위한 오퍼레이션만 등록한다
			// 다른 동작은 IllegalArgumentException 를 throwing 한다.
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("채팅 서버 프로그램 입니다. 클라이언트를 기다리고 있습니다...");

			/**
			 * non blocking 동작이기 때문에 클라이언트 별 스레드를 생성하지 않고 싱글 스레드로 동작한다
			 * 다수의 클라이언트가 접속하더라도 추가 스레드 생성이 없어 성능에 훨씬 유리하다
			 */
			while (true) {
				if (selector.select() == 0) {
					continue;
				}

				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					// 같은 키가 반복되는 것을 막기 위해 처리한 키는 제거한다.
					keys.remove();
					// 키의 유효성을 체크한다
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						acceptHandle(key, selector);
					} else if (key.isReadable()) {
						readHandle(key);
					} else if (key.isWritable()) {
						writeHandle(key);
					}
				}
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

	private void readHandle(SelectionKey key) throws IOException, ClassNotFoundException {
		try {
			SocketChannel channel = (SocketChannel)key.channel();
			// 클라이언트에게 request를 받기 위해 InputStream을 소켓 채널로로부터 연다
			ChattingRequest chattingRequest = JsonRequestResponseConverter.getChattingRequest(channel, buffer);

			ChattingResponse response = new ChattingResponse();
			response.setMessageType(chattingRequest.getMessageType());
			String nickName = chattingRequest.getKey();
			ChattingRequest.MessageType type = chattingRequest.getMessageType();

			if (type == ChattingRequest.MessageType.JOIN) {
				if (registClient(nickName, channel)) {
					response.setSuccess(true);
					response.setNickName(nickName);
					response.setMessage(nickName + "님이 입장 하였습니다.");
				} else {
					response.setNickName("");
					response.setMessage(nickName + "은 이미 등록되어 있는 닉네임 입니다. 닉네임을 다시 입력해 주세요");
					response.setSuccess(false);
					// 이미 등록된 닉네임일 경우 클라이언트로 등록하지 않고 메세지를 바로 보낸다
					JsonRequestResponseConverter.writeResponse(response, channel);
					return;
				}
			} else if(type == ChattingRequest.MessageType.MESSAGE) {
				if (nickName != null) {
					response.setSuccess(true);
					response.setNickName(nickName);
					response.setMessage(chattingRequest.getMessage());
					System.out.println(nickName + ": " + chattingRequest.getMessage());
				} else {
					response.setMessage("닉네임을 먼저 등록해야 합니다.");
					response.setNickName("");
					response.setSuccess(false);
				}
			} else if (type == ChattingRequest.MessageType.LEAVE) {
				response.setSuccess(true);
				response.setNickName(nickName);
				response.setMessage("방을 나가셨습니다.");
				removeClient(key);
			} else {
				return;
			}
			// write를 할 수 있도록 list에 add 한다
			writeResponseList.add(response);

			// read 후에 writer를 위한 관심 오퍼레이션을 변경한다
			key.interestOps(SelectionKey.OP_WRITE);
		} catch(Exception e) {
			removeClient(key);
		}
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
				JsonRequestResponseConverter.writeResponse(response, socketChannel);
			} catch (IOException e) {
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
