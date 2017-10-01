package kr.ac.kunsan.network.chatting.second.nio.wrapio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ChattingClient {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("IP 주소와 포트를 전달하지 않았으므로 기본값인 IP: 127.0.0.1 PORT: 8080 으로 접속합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar ChattingClient 127.0.0.1 8080");
			args = new String[]{"127.0.0.1", "8080"};
		}
		System.out.println("채팅 프로그램을 시작합니다. 종료를 원할 시에는 서버에 입장후에 언제든지 !q 를 입력하세요.");

		/**
		 * 소켓을 열어준다
		 */
		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		SocketChannel socket = SocketChannel.open();

		if (socket.isOpen()) {
			socket.configureBlocking(true);
			if (socket.connect(new InetSocketAddress(host, port))) {
				System.out.println("서버에 접속 되었습니다.");
				System.out.print("대화명을 입력하세요: ");
				/**
				 * 클라이언트 채팅 프로그램을 위한 핸들러를 신규 스레드로 수행한다.
				 */
				new ClientHandler(socket).start();
			}
		} else {
			System.out.println("socket이 연결되지 않았습니다. 종료 합니다.");
		}


	}
}
