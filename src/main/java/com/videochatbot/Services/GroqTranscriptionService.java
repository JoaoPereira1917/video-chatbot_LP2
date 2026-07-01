package com.videochatbot.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.util.List;

@Service
public class GroqTranscriptionService {

    private final RestClient restClient;
    private final String apiKey;

    public GroqTranscriptionService(@Value("${groq.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }


    public String transcribe(File audioFile) {

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(audioFile))
                .header("Content-Disposition", "form-data; name=file; filename=" + audioFile.getName());
        bodyBuilder.part("model", "whisper-large-v3-turbo");
        bodyBuilder.part("response_format", "verbose_json"); //retorno de segmentos do video
        bodyBuilder.part("language", "pt");


        return restClient.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(String.class);
    }
}