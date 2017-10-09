package kr.ac.kunsan.network.filetransfer._1.client;

import java.io.*;
import java.net.Socket;

import kr.ac.kunsan.network.NetworkUtils;

public class ClientHandler extends Thread {
	private InputStream inputStream;
	private OutputStream outputStream;
	private Socket socket;
	private BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

	public ClientHandler(Socket socket) throws IOException {
		this.socket = socket;

		// 서버에게 response를 받기 위해 InputStream을 소켓으로부터 연다
		inputStream = socket.getInputStream();		// 서버에게 request를 보내기 위해 InputStream을 소켓으로부터 연다
		outputStream = socket.getOutputStream();
	}

	@Override
	public void run() {
		System.out.print("전송할 파일 경로를 입력하세요: ");

		try {
			InputStream fileStream = new BufferedInputStream(new FileInputStream(keyboard.readLine()));
			OutputStream outputStream = this.outputStream;

			NetworkUtils.copyOutputStream(fileStream, outputStream);

			System.out.println("파일 전송이 완료 되었습니다.");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeAllOfCloseableResources();
		}

	}

	/**
	 * 사용된 모든 자원을 정리한다
	 */
	private void closeAllOfCloseableResources() {
		NetworkUtils.closeQuietly(keyboard);
		NetworkUtils.closeQuietly(outputStream);
		NetworkUtils.closeQuietly(inputStream);
		NetworkUtils.closeQuietly(socket);
	}
}
