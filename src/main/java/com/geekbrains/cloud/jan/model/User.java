package com.geekbrains.cloud.jan.model;

public class User {
    public String id = "";
    public String nick = "";
    public User(String id, String name) {
        this.id = id;
        this.nick = name;
    }

    public String getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }
}
