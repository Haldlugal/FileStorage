package com.geekbrains.cloud.jan.model;

public class LoginResponse implements CloudMessage{

    String nickName;
    boolean success;

    public LoginResponse(String nickName, boolean success) {
        this.nickName = nickName;
        this.success = success;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN_RESPONSE;
    }

    public String getNickName() {
        return nickName;
    }

    public boolean isSuccess() {
        return success;
    }
}
