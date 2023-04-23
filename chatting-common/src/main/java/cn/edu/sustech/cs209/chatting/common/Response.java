package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Response implements Serializable {

    public RequestType responseType;
    private Object obj;

    public Response(RequestType responseType, Object obj) {
        this.responseType = responseType;
        this.obj = obj;
    }

    public Object getObj() {
        return obj;
    }
}
