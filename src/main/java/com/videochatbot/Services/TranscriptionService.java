package com.videochatbot.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videochatbot.Dtos.GroqResponse;
import com.videochatbot.Dtos.GroqSegment;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class TranscriptionService {

    private final GroqTranscriptionService groqService;
    private final ProcessContextService processContextService;
    private final ObjectMapper objectMapper;
    private final AudioProcessService audioProcessService;

    private  String tempDir;

    public TranscriptionService(GroqTranscriptionService groqService, ProcessContextService processContextService,
                                ObjectMapper objectMapper, AudioProcessService audioProcessService,
                                @Value("${java.io.tmpdir}") String tempDir) {

        this.groqService = groqService;
        this.processContextService = processContextService;
        this.objectMapper = objectMapper;
        this.audioProcessService = audioProcessService;
        this.tempDir = tempDir;
    }
    public void processarVideo(String url){
        try {
            File arquivoFlac = audioProcessService.getAudio(url);
            if (arquivoFlac == null) {
                throw new RuntimeException("Falha ao obter audio");
            }
            String jsonTranscricao = groqService.transcribe(arquivoFlac);
            List<GroqSegment> segmentos = parseTranscricao(jsonTranscricao);
            salvarSegmentos(segmentos, url);
            audioProcessService.deletarArquivo(arquivoFlac);
        }
        catch (Exception e) {

            throw new RuntimeException("Erro no processamento do vídeo", e);
        }

    }
    private List<GroqSegment> parseTranscricao(String json){
        try{
            GroqResponse response = objectMapper.readValue(json, GroqResponse.class);
            return response.segments();
        }
        catch(JsonProcessingException e){
            throw new IllegalArgumentException("Erro ao parsear JSON da transcrição", e);
        }
    }
    private void salvarSegmentos(List<GroqSegment> segmentos, String origem){
        processContextService.limparBanco();
        List<Document> documentos = new ArrayList<>();
        for(GroqSegment seg : segmentos){
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "video");
            metadata.put("title", origem);
            metadata.put("start", seg.start());
            metadata.put("end", seg.end());
            Document doc = new Document(seg.text(), metadata);
            documentos.add(doc);
        }
        processContextService.storeDocuments(documentos);
    }

}
