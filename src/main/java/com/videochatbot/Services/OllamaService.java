package com.videochatbot.Services;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaService {

    private final OllamaChatModel chatModel;

    public OllamaService(OllamaChatModel chatModel){
        this.chatModel = chatModel;
    }



}