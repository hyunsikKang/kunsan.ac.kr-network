package kr.ac.kunsan.network.filetransfer;

import java.io.Serializable;

public class FileResponse implements Serializable {
	private static final long serialVersionUID = -6150701273118412086L;
	private String savePath;
	private boolean success;

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
