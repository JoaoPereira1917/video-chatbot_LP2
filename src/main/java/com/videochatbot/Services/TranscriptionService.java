package com.videochatbot.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videochatbot.Dtos.GroqResponse;
import com.videochatbot.Dtos.GroqSegment;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class TranscriptionService {

    private final GroqTranscriptionService groqService;
    private final ProcessContextService processContextService;
    private final ObjectMapper objectMapper;
    @Value("${java.io.tmpdir}")
    private String tempDir;

    public TranscriptionService(GroqTranscriptionService groqService, ProcessContextService processContextService,
        ObjectMapper objectMapper, @Value("${app.audio.temp-dir}") String tempDir) {

        this.groqService = groqService;
        this.processContextService = processContextService;
        this.objectMapper = objectMapper;
        this.tempDir = tempDir;
    }
    public void processarVideo(String url){
        try {
            File arquivoFlac = GetAudio(url);
            if (arquivoFlac == null) {
                throw new RuntimeException("Falha ao obter audio");
            }
            String jsonTranscricao = groqService.transcribe(arquivoFlac);
            List<GroqSegment> segmentos = parsearTranscricao(jsonTranscricao);
            salvarSegmentos(segmentos, url);
            deletarArquivo(arquivoFlac);
        }
        catch (Exception e) {
            System.err.println(" Erro no processamento: " + e.getMessage());

        }

    }
    private List<GroqSegment> parsearTranscricao(String json){
        try{
            GroqResponse response = objectMapper.readValue(json, GroqResponse.class);
            return response.segments();
        }
        catch(JsonProcessingException e){
            throw new IllegalArgumentException("Erro ao parsear JSON da transcrição", e);
        }
    }
    private void salvarSegmentos(List<GroqSegment> segmentos, String origem){

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

    protected File GetAudio(String url) {
        try {

            String nomeBase = "audio_baixado";
            List<String> downloadCmd = new ArrayList<>();
            downloadCmd.add("yt-dlp");
            downloadCmd.add("-f");
            downloadCmd.add("bestaudio[ext=m4a]/bestaudio");
            downloadCmd.add("-o");
            downloadCmd.add(nomeBase + ".%(ext)s");
            downloadCmd.add(url);

            System.out.println("Iniciando download com: " + String.join(" ", downloadCmd));
            executarComando(downloadCmd);


            File arquivoBaixado = localizarArquivo(nomeBase, tempDir);
          if (arquivoBaixado == null) {
              System.err.println("Erro: Arquivo baixado não encontrado.");
             throw new NullPointerException("");
          }
            //converter
            String arquivoSaida = "audio_final.flac";
            List<String> convertCmd = new ArrayList<>();
            convertCmd.add("ffmpeg");
            convertCmd.add("-y");
            convertCmd.add("-i");
            convertCmd.add(arquivoBaixado.getAbsolutePath());
            convertCmd.add("-ar");
            convertCmd.add("16000");
            convertCmd.add("-ac");
            convertCmd.add("1");
            convertCmd.add("-map");
            convertCmd.add("0:a");
            convertCmd.add("-c:a");
            convertCmd.add("flac");
            convertCmd.add(arquivoSaida);

            System.out.println("Convertendo para FLAC: " + String.join(" ", convertCmd));
            executarComando(convertCmd);


            return localizarArquivo(arquivoSaida, tempDir);

        } catch (IOException e) {
            System.err.println("Erro de IO: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processo interrompido.");
        }
        return null;
    }
    private void executarComando(List<String> comando) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Lê a saída em tempo real
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Comando falhou com código " + exitCode + ": " + String.join(" ", comando));
        }
    }
    private File localizarArquivo(String nomeArquivo, String diretorio) {
        File dir = new File(diretorio);

        File[] matches = dir.listFiles((d, name) -> name.startsWith(nomeArquivo + "."));
        if (matches != null && matches.length > 0) {

            return matches[0];
        }
        return null;
    }
    private void deletarArquivo(File arquivo) {
        if (arquivo != null && arquivo.exists()) {
            boolean apagado = arquivo.delete();
            if (!apagado) {
                System.err.println("Não foi possível deletar " + arquivo.getAbsolutePath());
            }
        }
    }
}
