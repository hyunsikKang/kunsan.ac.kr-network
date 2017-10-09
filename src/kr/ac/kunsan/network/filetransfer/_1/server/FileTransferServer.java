package kr.ac.kunsan.network.filetransfer._1.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer {
	private ServerSocket serverSocket;
	private String baseDirectory = System.getProperty("java.io.tmpdir") + "/";

	public FileTransferServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("포트를 전달하지 않았으므로 기본값인 PORT: 8080 으로 서버를 생성합니다.");
			System.out.println("IP PORT 를 전달하세요, example: java -jar ChattingServer 8080");
			args = new String[] {"8080"};
		}

		/**
		 * 채팅 서버를 생성한다
		 */
		FileTransferServer server = new FileTransferServer(Integer.valueOf(args[0]));
		System.out.println("파일 전송 프로그램 입니다. 클라이언트를 기다리고 있습니다...");

		while (true) {
			/**
			 * 클라이언트를 기다린다.
			 * 소켓이 접속되면 신규 스레드를 생성하여 서버가 지속적으로 클라이언트의 접속을 받을 수 있도록 한다.
			 */
			Socket acceptSocket = server.serverSocket.accept();
			InetSocketAddress remoteSocketAddress = (InetSocketAddress)acceptSocket.getRemoteSocketAddress();
			String ip = remoteSocketAddress.getAddress().getHostAddress();
			int port = acceptSocket.getPort();
			System.out.println("클라이언트가 접속 하였습니다. IP : " + ip + ", PORT : " + port);

			new ServerReceiveSocketHandler(acceptSocket, server, server.baseDirectory + ip + "/" + port).start();
		}
	}

}
