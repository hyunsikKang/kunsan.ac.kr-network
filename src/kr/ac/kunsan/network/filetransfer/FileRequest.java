package kr.ac.kunsan.network.filetransfer;

import java.io.Serializable;

public class FileRequest implements Serializable {
	private static final long serialVersionUID = 7334228436207553952L;
	private String fileName;
	private long fileSize;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

}
