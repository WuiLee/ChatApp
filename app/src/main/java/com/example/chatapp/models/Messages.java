package com.example.chatapp.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Messages
{
    public String from, message, type, to, messageID, time, date, name;

    public Messages() {}

    public Messages(String from, String message, String type, String to, String messageID, String time, String date, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("from", from);
        result.put("message", message);
        result.put("type", type);
        result.put("to", to);
        result.put("messageID", messageID);
        result.put("time", time);
        result.put("date", date);
        result.put("name", name);
        return result;
    }

    @Exclude
    public Boolean isSentBy(String uid) {
        return uid.equals(from);
    }
}
