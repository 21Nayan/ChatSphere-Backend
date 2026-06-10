package com.chatsphere.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    // We inject ChatClient.Builder here. It is the modern, more stable way to use Spring AI.
    public AiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String getAiResponse(String prompt) {
        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            // Log the actual error to the console so you can still debug it behind the scenes
            System.err.println("--- AI API ERROR ---");
            if (e.getCause() != null) {
                System.err.println("REASON: " + e.getCause().getMessage());
            } else {
                e.printStackTrace();
            }

            // Return a recruiter-friendly fallback message to the frontend UI
            return "I am currently taking a quick nap to recharge my API quotas! 😴 Please check back later or reach out to my developer.";
        }
    }
}