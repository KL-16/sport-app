package com.joinme.chatapp.utilities;

import java.util.HashMap;

public class Constants {

    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_AGE = "age";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_LAST_SENDER_ID = "lastSenderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SEEN = "seen";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_SENDER_AGE = "senderAge";
    public static final String KEY_RECEIVER_AGE = "receiverAge";
    public static final String KEY_SENDER_GENDER = "senderGender";
    public static final String KEY_RECEIVER_GENDER = "receiverGender";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_SENDER_BIO = "senderBio";
    public static final String KEY_RECEIVER_BIO = "receiverBio";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LOGIN_TIME = "loginTime";
    public static final String KEY_SHORT_BIO = "shortBio";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_EVENT_DESCRIPTION = "eventDescription";
    public static final String KEY_COLLECTION_EVENTS = "events";
    public static final String KEY_EVENT_LATITUDE = "Elatitude";
    public static final String KEY_EVENT_LONGITUDE = "Elongitude";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA9mu5Izk:APA91bHjmz-MAstGrtdwZnHa6j9_PwG-jmNluQ0V9TX4POqr5VU1RRXLpobbXr7PCJLvV02caGqIFnIpFu-iNLZKZKQXLGVeRiaqhHJDHRRZsPACgyUltlmFGPC9oWdTH00qEGV5BwUB"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE, "application/json"
            );
        }
        return remoteMsgHeaders;
    }

}
