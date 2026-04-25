package com.yinfu.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatRequest {
    private List<Message> messages;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}