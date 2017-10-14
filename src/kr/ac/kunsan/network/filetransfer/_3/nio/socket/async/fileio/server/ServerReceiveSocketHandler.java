package kr.ac.kunsan.network.filetransfer._3.nio.socket.async.fileio.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
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

public class ServerReceiveSocketHandler extends Thread {
	private SocketChannel socket;
	private String writePath;

	ServerReceiveSocketHandler(SocketChannel socket, String writeDirectory) throws IOException {
		this.socket = socket;
		new File(writeDirectory).mkdirs();
		writePath = writeDirectory;
	}

	@Override
	public void run() {
		try {
			// 서버에서 클라이언트와 통신할 소켓의 input Stream을 열어준다
			DataInputStream inputStream = new DataInputStream(SocketChannelStream.in(socket));

			FileRequest request = JsonRequestResponseConverter.fromRequestString(inputStream.readUTF());
			// 저장할 파일의 버퍼 스트림을 열어준다
			String savePath = writePath + File.separator + request.getFileName();

			try (AsynchronousFileChannel seekableByteChannel = AsynchronousFileChannel.open(Paths.get(savePath), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
				// Kernel의 DMA를 JVM 버퍼 없이 이용하는 DirectByteBuffer를 생성한다
				ByteBuffer bf = ByteBuffer.allocateDirect(1024);

				long count = 0;
				long read;
				// 파일 사이즈만큼 읽어들인다.
				while (count < request.getFileSize() && (read = socket.read(bf)) > 0) {
					count += read;
					bf.flip();
					seekableByteChannel.write(bf, count).get();
					bf.clear();
				}
			}
			FileResponse response = new FileResponse();
			response.setSavePath(savePath);
			response.setSuccess(true);

			BufferedOutputStream bos = new BufferedOutputStream(SocketChannelStream.out(socket));
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeUTF(JsonRequestResponseConverter.toString(response));
			dos.flush();
			System.out.println(savePath + " 경로에 저장 완료 되었습니다.");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			NetworkUtils.closeQuietly(socket);
		}
	}

}
