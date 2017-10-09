package kr.ac.kunsan.network.chatting._3.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.JsonRequestResponseConverter;

public class SocketRequestResponseUtils {
	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
	public static final CharsetDecoder DECODER = DEFAULT_CHARSET.newDecoder();

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

	public static String readStringFromSocketChannel(SocketChannel socket, ByteBuffer buffer) throws IOException {
		StringBuilder resp = new StringBuilder();
		int count;
		while (true) {
			count = socket.read(buffer);
			/**
			 * -1 은 연결에 문제가 발생 했을때 리턴 된다
			 */
			if (count == -1) {
				buffer.clear();
				throw new IOException("connection closed");
			}
			/**
			 * non blocking 소켓 채널의 경우 데이터를 받을것이 없다면 0이 리턴된다
			 */
			if (count == 0) {
				break;
			}
			// 읽어들인 버퍼를 초기화 하여 문자열로 변환할 준비를 한다
			buffer.flip();

			// 버퍼에서 디코더로 데이터를 읽어 들여 문자열로 변환한다
			resp.append(DECODER.decode(buffer).toString());

			if (buffer.hasRemaining()) {
				buffer.compact();
			} else {
				buffer.clear();
				if (socket.isBlocking()) {
					break;
				}
			}


		}
		return resp.toString();
	}
}
