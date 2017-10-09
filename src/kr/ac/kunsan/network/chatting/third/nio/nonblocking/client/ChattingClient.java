package kr.ac.kunsan.network.chatting.third.nio.nonblocking.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ChattingClient {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("IP 주소와 포트를 전달하지 않았으므로 기본값인 IP: 127.0.0.1 PORT: 8080 으로 접속합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar ChattingClient 127.0.0.1 8080");
			args = new String[] {"127.0.0.1", "8080"};
		}
		System.out.println("채팅 프로그램을 시작합니다. 종료를 원할 시에는 서버에 입장후에 언제든지 !q 를 입력하세요.");

		/**
		 * 소켓을 열어준다
		 */
		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		SocketChannel socket = SocketChannel.open();
		Selector selector = Selector.open();

		if (socket.isOpen() && selector.isOpen()) {
			socket.configureBlocking(false);
			// 오픈된 소켓은 접속에 대해서만 셀렉트 키를 등록한다
			// 이후에는 키 입력과 서버로부터 수신을 별도로 받아야 하므로
			// 셀렉터의 사용 없이 읽기와 보내기를 별도의 스레드에서 처리한다
			socket.register(selector, SelectionKey.OP_CONNECT);

			// 접속을 수행하지만 곧바로 접속되지 않고 non blocking 형태로 곧바로 리턴된다
			socket.connect(new InetSocketAddress(host, port));

			while (selector.select(1000) > 0) {
				Set<SelectionKey> keys = selector.selectedKeys();

				Iterator<SelectionKey> iterator = keys.iterator();

				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();

					SocketChannel connectedChannel = (SocketChannel)key.channel();
					if (!key.isConnectable()) {
						continue;
					}

					System.out.println("서버에 접속 되었습니다.");
					System.out.print("대화명을 입력하세요: ");
					// 접속 대기중인 상태가 있다면 완료 처리후 접속한다
					if (connectedChannel.isConnectionPending()) {
						connectedChannel.finishConnect();
					}

					/**
					 * 클라이언트 채팅 프로그램을 위한 핸들러를 신규 스레드로 수행한다.
					 * non blocking 채널이라고 꼭 single thread로 동작할 필요는 없다
					 * 키 입력과 수신을 별도로 진행해야 하니 내부에서 그대로 스레드를 사용한다
					 */
					new ClientHandler(connectedChannel).start();
				}
			}
		} else {
			System.out.println("socket이 연결되지 않았습니다. 종료 합니다.");
		}

	}
}
