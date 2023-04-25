package cn.edu.sustech.cs209.chatting.common;

import java.io.File;
import java.io.Serializable;

public class UploadedFile implements Serializable {

    private final Long timestamp;
    private final User sentBy;
//    private final Chat sendTo;
    private File file;

    public UploadedFile(Long timestamp, User sentBy, File file) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
//        this.sendTo = sendTo;
        this.file = file;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public User getSentBy() {
        return sentBy;
    }

    public File getFile() {
        return file;
    }
}
