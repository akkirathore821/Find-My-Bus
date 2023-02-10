package com.example.findmybus;

public class User {

    private String avatar;
    //todo
    private String name;
    private String email;
    private String id;

    public User(String avatar, String name, String email, String id) {
        this.avatar = avatar;
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public User(String name, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
