package cn.edu.sustech.cs209.chatting.common;

public class Message {

//    private final Long timestamp;
//    private final String sentBy;
//    private final String sendTo;
//    private final String data;

    private final Long timestamp;
    private final User sentBy;
    private final Chat sendTo;
    private final String content;

    public Message(Long timestamp, User sentBy, Chat sendTo, String content) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public User getSentBy() {
        return sentBy;
    }

    public Chat getSendTo() {
        return sendTo;
    }

    public String getContent() {
        return content;
    }
}
