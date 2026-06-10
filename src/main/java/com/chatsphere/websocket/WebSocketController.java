package com.chatsphere.websocket;

import com.chatsphere.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService           chatService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                                ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService       = chatService;
    }

    // ── Typing indicator ──────────────────────────────────────
    // Frontend publishes to: /app/chat.typing
    // Payload: { roomId?, dmKey?, typing: true/false }
    // Backend rebroadcasts to: /topic/typing.{roomId} or /topic/typing.dm.{dmKey}
    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload, Principal principal) {
        payload.put("username", principal.getName());
        if (payload.containsKey("roomId")) {
            messagingTemplate.convertAndSend(
                "/topic/typing." + payload.get("roomId"), payload);
        } else if (payload.containsKey("dmKey")) {
            messagingTemplate.convertAndSend(
                "/topic/typing.dm." + payload.get("dmKey"), payload);
        }
    }

    // ── Presence — user joined ────────────────────────────────
    // Frontend publishes to: /app/user.join
    @MessageMapping("/user.join")
    public void userJoin(@Payload Map<String, Object> payload, Principal principal) {
        payload.put("username", principal.getName());
        payload.put("event",    "join");
        messagingTemplate.convertAndSend("/topic/presence", payload);
    }

    // ── Presence — user left ──────────────────────────────────
    // Frontend publishes to: /app/user.leave
    @MessageMapping("/user.leave")
    public void userLeave(@Payload Map<String, Object> payload, Principal principal) {
        payload.put("username", principal.getName());
        payload.put("event",    "leave");
        messagingTemplate.convertAndSend("/topic/presence", payload);
    }
}