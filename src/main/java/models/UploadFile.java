package models;

import java.io.Serializable;

public class UploadFile implements Serializable {

    private String fileName;
    private String fileType;
    private byte[] fileData;

    public UploadFile(String fileName, String fileType, byte[] fileData) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
