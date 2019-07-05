package com.example.chatapp.models;

public class Profile {
    public String uid;
    public String userId;
    public String userToken;
    public String courseId;
    public String identity;
    public String name;
    public String phoneNumber;

    public Profile(){}

    public Profile(String uid,
                   String userId,
                   String userToken,
                   String courseId,
                   String identity,
                   String name,
                   String phoneNumber) {
        this.uid = uid;
        this.userId = userId;
        this.userToken = userToken;
        this.courseId = courseId;
        this.identity = identity;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
