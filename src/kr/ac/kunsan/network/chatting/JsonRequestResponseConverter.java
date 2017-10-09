package kr.ac.kunsan.network.chatting;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class JsonRequestResponseConverter {

	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
	public static final CharsetDecoder DECODER = DEFAULT_CHARSET.newDecoder();

	public static String toString(ChattingRequest request) {
		return Json.object()
			.add("key", request.getKey())
			.add("message", request.getMessage() != null ? request.getMessage() : "")
			.add("messageType", request.getMessageType().name())
			.toString();
	}
	public static String toString(ChattingResponse response) {
		return Json.object()
			.add("message", response.getMessage())
			.add("messageType", response.getMessageType().name())
			.add("nickName", response.getNickName())
			.add("success", response.isSuccess())
			.toString();
	}

	public static ChattingRequest fromRequestString(String request) {
		if (request == null || request.isEmpty()) {
			return null;
		}

		JsonObject value = Json.parse(request).asObject();
		ChattingRequest chattingRequest = new ChattingRequest();
		chattingRequest.setKey(value.getString("key", null));
		chattingRequest.setMessage(value.getString("message", null));
		chattingRequest.setMessageType(ChattingRequest.MessageType.valueOf(value.getString("messageType", "")));

		return chattingRequest;
	}

	public static ChattingResponse fromResponseString(String request) {
		if (request == null || request.isEmpty()) {
			return null;
		}
		JsonObject value = Json.parse(request).asObject();
		ChattingResponse chattingRequest = new ChattingResponse();
		chattingRequest.setNickName(value.getString("nickName", null));
		chattingRequest.setMessage(value.getString("message", null));
		chattingRequest.setMessageType(ChattingRequest.MessageType.valueOf(value.getString("messageType", ChattingRequest.MessageType.MESSAGE.name())));
		chattingRequest.setSuccess(value.getBoolean("success", false));

		return chattingRequest;
	}
}
