package kr.ac.kunsan.network.filetransfer;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class JsonRequestResponseConverter {
	public static String toString(FileRequest request) {
		return Json.object()
			.add("fileName", request.getFileName())
			.add("fileSize", request.getFileSize())
			.toString();
	}
	public static String toString(FileResponse response) {
		return Json.object()
			.add("savePath", response.getSavePath())
			.add("success", response.isSuccess())
			.toString();
	}

	public static FileRequest fromRequestString(String request) {
		if (request == null || request.isEmpty()) {
			return null;
		}

		JsonObject value = Json.parse(request).asObject();
		FileRequest fileRequest = new FileRequest();
		fileRequest.setFileName(value.getString("fileName", null));
		fileRequest.setFileSize(value.getLong("fileSize", 0L));

		return fileRequest;
	}

	public static FileResponse fromResponseString(String request) {
		if (request == null || request.isEmpty()) {
			return null;
		}
		JsonObject value = Json.parse(request).asObject();
		FileResponse chattingRequest = new FileResponse();
		chattingRequest.setSavePath(value.getString("savePath", null));
		chattingRequest.setSuccess(value.getBoolean("success", false));

		return chattingRequest;
	}
}
