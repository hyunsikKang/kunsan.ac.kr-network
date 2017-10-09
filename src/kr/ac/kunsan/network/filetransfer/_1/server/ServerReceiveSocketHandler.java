package kr.ac.kunsan.network.filetransfer._1.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import kr.ac.kunsan.network.NetworkUtils;

public class ServerReceiveSocketHandler extends Thread {
	FileTransferServer server;
	private Socket socket;
	private String writePath;

	ServerReceiveSocketHandler(Socket socket, FileTransferServer server, String writeDirectory) throws IOException {
		this.socket = socket;
		this.server = server;
		new File(writeDirectory).mkdirs();
		writePath = writeDirectory + "/" + UUID.randomUUID().toString();
	}

	@Override
	public void run() {
		try {
			// 서버에서 클라이언트와 통신할 소켓의 input/output Stream을 열어준다
			InputStream inputStream = socket.getInputStream();
			try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(writePath))) {
				NetworkUtils.copyOutputStream(inputStream, outputStream);
			}

			System.out.println(writePath + " 경로에 저장 완료 되었습니다.");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			NetworkUtils.closeQuietly(socket);
		}
	}

}
