package com.videochatbot.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TranscriptionService {
    private String url = "https://www.youtube.com/watch?v=bOkP-LmmMZs&list=RDbOkP-LmmMZs&start_radio=1";

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

            List<String> command = new ArrayList<>();
            command.add("yt-dlp");
            command.add("-x");
            command.add("--audio-format");
            command.add("mp3");
            command.add("--audio-quality");
            command.add("0");
            command.add(getUrl());

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);

            System.out.println("Iniciando download com: " + String.join(" ", command));

            Process process = processBuilder.start();


            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Sucesso: Áudio extraído!");
            } else {
                System.err.println("Erro: yt-dlp falhou com código de saída " + exitCode);
            }

        } catch (IOException e) {
            System.err.println("Erro de IO (Verifique se o yt-dlp está instalado e no PATH): " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processo interrompido.");
        }
    }
}
