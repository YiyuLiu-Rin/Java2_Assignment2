package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Request implements Serializable {

    public RequestType requestType;
    private User user;  // 发出请求的客户端对应的用户

    public Request(RequestType requestType, User user) {
        this.requestType = requestType;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public enum RequestType implements Serializable {
        LOG_IN, SIGN_UP,
        GET_USER_LIST, GET_CHAT_LIST, GET_ONLINE_AMOUNT,  // 不断发送以更新
        C
    }
}
