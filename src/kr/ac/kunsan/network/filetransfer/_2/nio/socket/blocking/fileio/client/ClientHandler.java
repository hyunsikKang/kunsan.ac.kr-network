package kr.ac.kunsan.network.filetransfer._2.nio.socket.blocking.fileio.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import kr.ac.kunsan.network.NetworkUtils;
import kr.ac.kunsan.network.SocketChannelStream;
import kr.ac.kunsan.network.filetransfer.FileRequest;
import kr.ac.kunsan.network.filetransfer.FileResponse;
import kr.ac.kunsan.network.filetransfer.JsonRequestResponseConverter;

public class ClientHandler extends Thread {
	private SocketChannel socket;
	private BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

	public ClientHandler(SocketChannel socket) throws IOException {
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.print("전송할 파일 경로를 입력하세요: ");

		try {
			String filePath = keyboard.readLine();
			// 읽기 전용 모드로 파일을 연다
			try (SeekableByteChannel channel = Files.newByteChannel(Paths.get(filePath), StandardOpenOption.READ)) {
				File file = new File(filePath);
				FileRequest request = new FileRequest();
				request.setFileName(file.getName());
				request.setFileSize(file.length());

				OutputStream os = SocketChannelStream.out(socket);
				DataOutputStream dos = new DataOutputStream(os);

				dos.writeUTF(JsonRequestResponseConverter.toString(request));
				os.flush();

				// Kernel의 DMA를 JVM 버퍼 없이 이용하는 DirectByteBuffer를 생성한다
				ByteBuffer bf = ByteBuffer.allocateDirect(1024);
				// 0이 아닐때까지 읽어 들인다
				while (channel.read(bf) > 0) {
					bf.flip();
					socket.write(bf);
					bf.clear();
				}

				try (DataInputStream inputStream = new DataInputStream(SocketChannelStream.in(socket))) {
					FileResponse response = JsonRequestResponseConverter.fromResponseString(inputStream.readUTF());
					if (response.isSuccess()) {
						System.out.println("파일 전송이 완료 되었습니다. 서버 저장 경로 :" + response.getSavePath());
					}
				}
			}
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
		NetworkUtils.closeQuietly(socket);
	}
}
