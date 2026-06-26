package com.videochatbot.Controllers;

import com.videochatbot.Services.OllamaService;
import com.videochatbot.Services.RetrievalService;
import com.videochatbot.Services.TranscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Importante para permitir requisições do frontend
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    private final TranscriptionService transcriptionService;
    private final RetrievalService retrievalService;
    private final OllamaService ollamaService;

    public ChatbotController(TranscriptionService transcriptionService,
                             RetrievalService retrievalService,
                             OllamaService ollamaService) {
        this.transcriptionService = transcriptionService;
        this.retrievalService = retrievalService;
        this.ollamaService = ollamaService;
    }

    @PostMapping("/video/processar")
    public ResponseEntity<String> processarVideo(@RequestBody Map<String, String> body) {
        log.info("Recebida requisição /video/processar com body: {}", body);

        try {
            String url = body.get("url");
            if (url == null || url.isBlank()) {
                log.warn("URL não informada");
                return ResponseEntity.badRequest().body("URL não informada");
            }

            log.info("Iniciando processamento do vídeo: {}", url);
            transcriptionService.processarVideo(url);
            log.info("Processamento concluído com sucesso para: {}", url);

            return ResponseEntity.ok("Video processado com sucesso");

        } catch (Exception e) {
            log.error("Erro ao processar vídeo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/chat/perguntar")
    public ResponseEntity<String> perguntar(@RequestBody Map<String, String> body) {
        log.info("Recebida pergunta: {}", body);

        try {
            String pergunta = body.get("pergunta");
            if (pergunta == null || pergunta.isBlank()) {
                log.warn("Pergunta vazia");
                return ResponseEntity.badRequest().body("Pergunta não informada");
            }

            // O retrievalService pode lançar exceção se não houver contexto
            String contexto = retrievalService.getContext(pergunta, "video");
            log.debug("Contexto recuperado ({} caracteres)", contexto.length());

            String resposta = ollamaService.perguntar(pergunta, contexto);
            log.debug("Resposta gerada: {}", resposta);

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            log.error("Erro ao processar pergunta: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Erro interno: " + e.getMessage());
        }
    }
}