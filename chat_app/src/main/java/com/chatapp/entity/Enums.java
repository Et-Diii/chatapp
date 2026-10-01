package com.chatapp.entity;

public final class Enums {
    private Enums() {}
    public enum UserStatus { ONLINE, OFFLINE }
    public enum RequestStatus { PENDING, ACCEPTED, REJECTED, CANCELLED }
    public enum ConversationType { PRIVATE }
    public enum MessageType { TEXT }
}
