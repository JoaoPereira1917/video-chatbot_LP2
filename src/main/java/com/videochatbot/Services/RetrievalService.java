package com.videochatbot.Services;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RetrievalService {
    private final VectorStore vectorStore;
    public RetrievalService(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }
    public String getContext(String pergunta, String topicoAtual){
        List<Document> resultado = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(pergunta)
                        .topK(6)
                        //.filterExpression("topicIndex <= " + indiceTopicoAtual)
                        .build()
        );
        return resultado.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }

}
