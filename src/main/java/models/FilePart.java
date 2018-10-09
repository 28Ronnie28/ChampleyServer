package models;

import java.io.Serializable;

public class FilePart implements Serializable{

    private byte[] fileBytes;
    private String fileName;

    public FilePart(byte[] fileBytes, String fileName) {
        this.fileBytes = fileBytes;
        this.fileName = fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public String getFileName() {
        return fileName;
    }
}
