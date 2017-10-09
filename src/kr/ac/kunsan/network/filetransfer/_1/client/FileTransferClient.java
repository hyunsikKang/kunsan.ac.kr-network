package kr.ac.kunsan.network.filetransfer._1.client;

import java.io.IOException;
import java.net.Socket;

public class FileTransferClient {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("IP 주소와 포트를 전달하지 않았으므로 기본값인 IP: 127.0.0.1 PORT: 8080 으로 접속합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar FileTransferClient 127.0.0.1 8080");
			args = new String[]{"127.0.0.1", "8080"};
		}
		System.out.println("파일 전송 프로그램을 시작합니다. ");

		/**
		 * 소켓을 열어준다
		 */
		Socket socket = new Socket(args[0], Integer.valueOf(args[1]));

		System.out.println("서버에 접속 되었습니다.");
		/**
		 * 클라이언트 채팅 프로그램을 위한 핸들러를 신규 스레드로 수행한다.
		 */
		new ClientHandler(socket).start();
	}
}
