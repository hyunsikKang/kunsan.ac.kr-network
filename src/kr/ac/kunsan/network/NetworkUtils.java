package kr.ac.kunsan.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class NetworkUtils {
	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
	public static final CharsetDecoder DECODER = DEFAULT_CHARSET.newDecoder();

	public static void writeAndFlush(ObjectOutputStream outputStream, Object object) throws IOException {
		outputStream.writeObject(object);
		outputStream.flush();
	}

	public static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch(Exception e) {

		}
	}

	public static long copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte buffer[] = new byte[2048];

		long count = 0;
		int n;
		while((n = inputStream.read(buffer)) != -1)  {
			count += n;
			outputStream.write(buffer, 0, n);
		}

		return count;
	}

	public static long copyStream(InputStream inputStream, OutputStream outputStream, long size) throws IOException {
		byte buffer[] = new byte[2048];

		long count = 0;
		int n = 0;
		while(count < size && (n = inputStream.read(buffer)) != -1)  {
			count += n;
			outputStream.write(buffer, 0, n);
		}

		return count;
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
