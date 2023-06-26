package com.joinme.chatapp.models;

import java.util.Date;

public class ChatMessage {

    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public boolean seen;
    public String conversationId, conversationName, conversationImage, conversationAge, conversationGender, conversationBio, lastSenderId;
}
