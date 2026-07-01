package com.videochatbot.Services;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ProcessContextService {

    private final TokenTextSplitter textSplitter;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;


    public ProcessContextService(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {

        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
        this.jdbcTemplate = jdbcTemplate;
    }
    public void processUpload(MultipartFile file){
        if(file == null || file.isEmpty() ){
            throw new IllegalArgumentException("O arquivo não pode ser vazio");
        }
        try{
            PagePdfDocumentReader pdfReader =  new PagePdfDocumentReader(file.getResource());
            List<Document> documentos = pdfReader.read();
            storeDocuments(documentos);

        }
        catch (Exception e) {
            throw new IllegalArgumentException("Erro ao processar o arquivo", e);
        }
    }
    public void processText(String texto, String origem){
        if(texto == null || texto.isBlank()){
            throw new IllegalArgumentException("A transcrição falhou, fala não detectada");
        }
        Document doc = new Document(texto, Map.of("source", "video",
                "title", origem));

        storeDocuments(List.of(doc));

    }

    public void storeDocuments(List<Document> documentos){
        List<Document> chunks = textSplitter.apply(documentos);
        vectorStore.add(chunks);
    }
    public void limparBanco() {
        jdbcTemplate.update("DELETE FROM vector_store");
    }

}
