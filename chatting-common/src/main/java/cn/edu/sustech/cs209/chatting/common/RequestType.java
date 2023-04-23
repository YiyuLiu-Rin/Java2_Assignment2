package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public enum RequestType implements Serializable {
    LOG_IN, SIGN_UP,
    GET_ONLINE_USER_LIST, GET_CHAT_LIST, GET_CURRENT_CHAT, GET_ONLINE_AMOUNT,  // 不断发送以更新
    CREAT_CHAT, SEND_MESSAGE, SEND_EMOJI, SEND_FILE,
    DISCONNECT
}
