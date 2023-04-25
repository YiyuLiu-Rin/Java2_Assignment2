package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {

    public RequestType requestType;

    private User user;  // 发出请求的客户端对应的用户
    private List<String> participantNames;  // 创建聊天时的用户列表
    private Chat chat;  // 指定的聊天
    private Message message;  // 待发送消息
    private UploadedFile uploadedFile;  // 待发送文件


    // 构造以下请求：LOG_IN, SIGN_UP
    public Request(RequestType requestType, User user) {
        this.requestType = requestType;
        this.user = user;
    }

    // 构造以下请求：GET_ONLINE_USER_LIST, GET_CHAT_LIST, GET_CURRENT_CHAT, GET_ONLINE_AMOUNT, DISCONNECT
    public Request(RequestType requestType) {
        this.requestType = requestType;
    }

    // 构造以下请求：CREAT_PRIVATE_CHAT, CREAT_GROUP_CHAT
    public Request(RequestType requestType, List<String> participantNames) {
        this.requestType = requestType;
        this.participantNames = participantNames;
    }

    // 构造以下请求：SEND_MESSAGE, SEND_EMOJI
    public Request(RequestType requestType, Chat chat, Message message) {
        this.requestType = requestType;
        this.chat = chat;
        this.message = message;
    }

    // 构造以下请求：SEND_FILE
    public Request(RequestType requestType, Chat chat, UploadedFile uploadedFile) {
        this.requestType = requestType;
        this.chat = chat;
        this.uploadedFile = uploadedFile;
    }

    // 构造以下请求：CHANGE_CURRENT_CHAT
    public Request(RequestType requestType, Chat chat) {
        this.requestType = requestType;
        this.chat = chat;
    }


    public User getUser() {
        return user;
    }

    public List<String> getParticipantNames() {
        return participantNames;
    }

    public Chat getChat() {
        return chat;
    }

    public Message getMessage() {
        return message;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }
}