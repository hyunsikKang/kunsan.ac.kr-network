package kr.ac.kunsan.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

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

	public static long copyOutputStream(InputStream fileStream, OutputStream outputStream) throws IOException {
		byte buffer[] = new byte[2048];

		long count = 0;
		int n;
		while((n = fileStream.read(buffer)) != -1)  {
			count += n;
			outputStream.write(buffer, 0, n);
		}

		outputStream.flush();

		return count;
	}
}
