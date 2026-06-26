package com.videochatbot.Controllers;

import com.videochatbot.Services.OllamaService;
import com.videochatbot.Services.RetrievalService;
import com.videochatbot.Services.TranscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatbotController {
    private final TranscriptionService transcriptionService;
    private final RetrievalService retrievalService;
    private final OllamaService ollamaService;

    public ChatbotController(TranscriptionService transcriptionService, RetrievalService retrievalService, OllamaService ollamaService) {
        this.transcriptionService = transcriptionService;
        this.retrievalService = retrievalService;
        this.ollamaService = ollamaService;
    }
    @PostMapping("/video/processar")
    public ResponseEntity<String> processarVideo(@RequestBody Map<String, String> body){

        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("URL não informada");
        }
        transcriptionService.processarVideo(url);
        return ResponseEntity.ok("Video processado com sucesso");
        //tratar exceção depois
    }
    @PostMapping("/chat/perguntar")
    public ResponseEntity<String> perguntar(@RequestBody Map<String, String> body){
        String pergunta = body.get("pergunta");
        String contexto = retrievalService.getContext(pergunta, "video");
        String resposta = ollamaService.perguntar(pergunta, contexto);
        return ResponseEntity.ok(resposta);
    }
}
