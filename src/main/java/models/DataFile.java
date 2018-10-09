package models;

import java.io.Serializable;

public class DataFile implements Serializable {

    private String fileType;
    private String fileName;
    private String fileExtension;
    private int fileLength;
    private int value;
    private Object fileDownloader;

    public DataFile(String fileType, String fileName, String fileExtension, int fileLength) {
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLength = fileLength;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public int getFileLength() {
        return fileLength;
    }

    public int getValue() {
        return value;
    }

    public Object getFileDownloader() {
        return fileDownloader;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setFileDownloader(Object fileDownloader) {
        this.fileDownloader = fileDownloader;
    }
}
