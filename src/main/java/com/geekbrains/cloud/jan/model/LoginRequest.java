package com.geekbrains.cloud.jan.model;

public class LoginRequest implements CloudMessage{

    private final String login;
    private final String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN_REQUEST;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

}
