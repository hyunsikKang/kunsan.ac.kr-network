package kr.ac.kunsan.network.chatting._4.nio.async.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import kr.ac.kunsan.network.chatting.ChattingRequest;
import kr.ac.kunsan.network.chatting.ChattingResponse;
import kr.ac.kunsan.network.chatting.JsonRequestResponseConverter;

public class FutureAsynchronousSocketRequestResponseUtils {
	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
	public static final CharsetDecoder DECODER = DEFAULT_CHARSET.newDecoder();

	public static void writeRequest(ChattingRequest request, AsynchronousSocketChannel socket) throws Exception {
		String req = JsonRequestResponseConverter.toString(request);

		writeStringRequest(socket, DEFAULT_CHARSET, req);
	}

	public static void writeResponse(ChattingResponse response, AsynchronousSocketChannel socket) throws Exception {
		String req = JsonRequestResponseConverter.toString(response);
		writeStringRequest(socket, DEFAULT_CHARSET, req);
	}

	private static void writeStringRequest(AsynchronousSocketChannel socket, Charset charset, String req) throws Exception {
		ByteBuffer buffer = ByteBuffer.wrap(req.getBytes(charset));
		while (buffer.hasRemaining()) {
			socket.write(buffer).get();
		}
	}

	public static ChattingResponse getChattingResponse(AsynchronousSocketChannel socket, ByteBuffer buffer) throws Exception {
		return JsonRequestResponseConverter.fromResponseString(readStringFromAsynchronousSocketChannel(socket, buffer));
	}

	public static ChattingRequest getChattingRequest(AsynchronousSocketChannel socket, ByteBuffer buffer) throws Exception {
		return JsonRequestResponseConverter.fromRequestString(readStringFromAsynchronousSocketChannel(socket, buffer));
	}

	public static String readStringFromAsynchronousSocketChannel(AsynchronousSocketChannel socket, ByteBuffer buffer) throws Exception {
		StringBuilder resp = new StringBuilder();
		int count;
		while ((count = socket.read(buffer).get()) != -1) {

			// 읽어들인 버퍼를 초기화 하여 문자열로 변환할 준비를 한다
			buffer.flip();

			// 버퍼에서 디코더로 데이터를 읽어 들여 문자열로 변환한다
			resp.append(DECODER.decode(buffer).toString());

			if (buffer.hasRemaining()) {
				buffer.compact();
			} else {
				buffer.clear();
				break;
			}
		}

		/**
		 * -1 은 연결에 문제가 발생 했을때 리턴 된다
		 */
		if (count == -1) {
			buffer.clear();
			throw new IOException("connection closed");
		}
		return resp.toString();
	}
}
