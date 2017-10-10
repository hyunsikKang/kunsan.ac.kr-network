package kr.ac.kunsan.network.chatting._3.nio;

import static kr.ac.kunsan.network.NetworkUtils.readStringFromSocketChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting.JsonRequestResponseConverter;

public class SocketRequestResponseUtils {
	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

	public static void writeRequest(ChattingRequest request, SocketChannel socket) throws IOException {
		String req = JsonRequestResponseConverter.toString(request);
		writeStringRequest(socket, DEFAULT_CHARSET, req);
	}

	public static void writeResponse(ChattingResponse response, SocketChannel socket) throws IOException {
		String req = JsonRequestResponseConverter.toString(response);
		writeStringRequest(socket, DEFAULT_CHARSET, req);
	}

	private static void writeStringRequest(SocketChannel socket, Charset charset, String req) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(req.getBytes(charset));
		while (buffer.hasRemaining()) {
			socket.write(buffer);
		}
	}

	public static ChattingResponse getChattingResponse(SocketChannel socket, ByteBuffer buffer) throws IOException {
		return JsonRequestResponseConverter.fromResponseString(readStringFromSocketChannel(socket, buffer));
	}

	public static ChattingRequest getChattingRequest(SocketChannel socket, ByteBuffer buffer) throws IOException {
		return JsonRequestResponseConverter.fromRequestString(readStringFromSocketChannel(socket, buffer));
	}
}
