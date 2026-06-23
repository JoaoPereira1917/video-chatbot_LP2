package com.videochatbot.Services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TranscriptionService {
    private String url = "https://youtu.be/lOjxZ7Spj58?si=kYTtZN_5Iy0zTR6G";

    public TranscriptionService() {
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void baixarAudio() {
        try {
            // 1. Download do melhor áudio com yt-dlp (formato original, geralmente webm/m4a)
            String nomeBase = "audio_baixado";  // nome base para os arquivos temporários
            List<String> downloadCmd = new ArrayList<>();
            downloadCmd.add("yt-dlp");
            downloadCmd.add("-f");
            downloadCmd.add("bestaudio[ext=m4a]/bestaudio"); // prioriza m4a, mas aceita qualquer bestaudio
            downloadCmd.add("-o");
            downloadCmd.add(nomeBase + ".%(ext)s");           // salva com a extensão original
            downloadCmd.add(getUrl());

            System.out.println("Iniciando download com: " + String.join(" ", downloadCmd));
            executarComando(downloadCmd);

            // 2. Localiza o arquivo baixado (precisa descobrir a extensão real)
            File arquivoBaixado = localizarArquivo(nomeBase);
            if (arquivoBaixado == null) {
                System.err.println("Erro: Arquivo baixado não encontrado.");
                return;
            }

            //converter
            String arquivoSaida = "audio_final.flac";
            List<String> convertCmd = new ArrayList<>();
            convertCmd.add("ffmpeg");
            convertCmd.add("-y");                   // sobrescrever saída se existir
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

            // 4. (Opcional) Excluir o arquivo intermediário
            // arquivoBaixado.delete();

            System.out.println("Sucesso! Arquivo FLAC criado: " + arquivoSaida);

        } catch (IOException e) {
            System.err.println("Erro de IO: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processo interrompido.");
        }
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
    private File localizarArquivo(String nomeBase) {
        File dir = new File(".");
        File[] matches = dir.listFiles((d, name) -> name.startsWith(nomeBase + "."));
        if (matches != null && matches.length > 0) {

            return matches[0];
        }
        return null;
    }
}
