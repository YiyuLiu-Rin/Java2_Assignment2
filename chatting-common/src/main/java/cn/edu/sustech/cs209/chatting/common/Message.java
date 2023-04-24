package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Message implements Serializable {

//    private final Long timestamp;
//    private final String sentBy;
//    private final String sendTo;
//    private final String data;

    private final Long timestamp;
    private final User sentBy;
//    private final Chat sendTo;
    private final String content;

    public Message(Long timestamp, User sentBy, String content) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
//        this.sendTo = sendTo;
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public User getSentBy() {
        return sentBy;
    }

    public String getContent() {
        return content;
    }
}
