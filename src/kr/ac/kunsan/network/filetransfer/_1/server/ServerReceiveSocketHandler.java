package kr.ac.kunsan.network.filetransfer._1.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import kr.ac.kunsan.network.NetworkUtils;
import kr.ac.kunsan.network.filetransfer.FileRequest;
import kr.ac.kunsan.network.filetransfer.FileResponse;
import kr.ac.kunsan.network.filetransfer.JsonRequestResponseConverter;

public class ServerReceiveSocketHandler extends Thread {
	private FileTransferServer server;
	private Socket socket;
	private String writePath;

	ServerReceiveSocketHandler(Socket socket, FileTransferServer server, String writeDirectory) throws IOException {
		this.socket = socket;
		this.server = server;
		new File(writeDirectory).mkdirs();
		writePath = writeDirectory;
	}

	@Override
	public void run() {
		try {
			// 서버에서 클라이언트와 통신할 소켓의 input Stream을 열어준다
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());

			FileRequest request = JsonRequestResponseConverter.fromRequestString(inputStream.readUTF());
			// 저장할 파일의 버퍼 스트림을 열어준다
			String savePath = writePath + File.separator + request.getFileName();
			try (OutputStream outputStream = new FileOutputStream(savePath)) {
				NetworkUtils.copyStream(socket.getInputStream(), outputStream, request.getFileSize());
			}
			FileResponse response = new FileResponse();
			response.setSavePath(savePath);
			response.setSuccess(true);

			BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
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
