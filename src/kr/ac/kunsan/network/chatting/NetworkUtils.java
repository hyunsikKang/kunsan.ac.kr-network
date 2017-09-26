package kr.ac.kunsan.network.chatting;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class NetworkUtils {
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
}
