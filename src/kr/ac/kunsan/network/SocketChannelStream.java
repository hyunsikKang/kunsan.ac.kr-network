package kr.ac.kunsan.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * https://github.com/jenkinsci/remoting/blob/master/src/main/java/hudson/remoting/SocketChannelStream.java
 * 위 주소의 Kohsuke Kawaguchi 의 코드를 가져와서 사용합니다.
 * 아래와 같은 설명이 원본 코드에 존재합니다. JDK 자체 버그로 JDK 1.8 144 버전까지 해결되지 않아서 차용합니다.
 * NIO의 소켓 채널에서 socket.socket().getInput/OutputStream() 메소드 사용시
 * 동시에 read/write가 일어날 경우 데드락이 걸리게 됩니다.
 * 그것을 방지하기 위한 wrapper 코드입니다.
 *
 * Wraps {@link SocketChannel} into {@link InputStream}/{@link OutputStream} in a way
 * that avoids deadlock when read/write happens concurrently.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="http://stackoverflow.com/questions/174774/">discussion with references to BugParade Bug IDs</a>
 */
public class SocketChannelStream {
	public static InputStream in(final SocketChannel ch) throws IOException {
		final Socket s = ch.socket();

		return Channels.newInputStream(new ReadableByteChannel() {
			public int read(ByteBuffer dst) throws IOException {
				return ch.read(dst);
			}

			public void close() throws IOException {
				if (!s.isInputShutdown()) {
					try {
						s.shutdownInput();
					} catch (IOException e) {

					}
				}
				if (s.isOutputShutdown()) {
					ch.close();
					s.close();
				}
			}

			public boolean isOpen() {
				return !s.isInputShutdown();
			}
		});
	}

	public static OutputStream out(final SocketChannel ch) throws IOException {
		final Socket s = ch.socket();

		return Channels.newOutputStream(new WritableByteChannel() {
			public int write(ByteBuffer src) throws IOException {
				return ch.write(src);
			}

			public void close() throws IOException {
				if (!s.isOutputShutdown()) {
					try {
						s.shutdownOutput();
					} catch (IOException e) {
					}
				}
				if (s.isInputShutdown()) {
					ch.close();
					s.close();
				}
			}

			public boolean isOpen() {
				return !s.isOutputShutdown();
			}
		});
	}

}