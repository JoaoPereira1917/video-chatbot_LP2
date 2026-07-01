package com.videochatbot.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



@Service
public class AudioProcessService {

    private String tempDir;

    public AudioProcessService(@Value("${app.audio.temp-dir}") String tempDir) {
        this.tempDir = tempDir;


        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(tempDir));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório temporário: " + tempDir, e);
        }
    }

    protected File getAudio(String url) {
        try {

            File tempFolder = new File(tempDir);
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            String nomeBase = "audio_baixado";


            List<String> downloadCmd = new ArrayList<>();
            downloadCmd.add("yt-dlp");
            downloadCmd.add("-f");
            downloadCmd.add("bestaudio[ext=m4a]/bestaudio");
            downloadCmd.add("-o");
            downloadCmd.add(nomeBase + ".%(ext)s");
            downloadCmd.add(url);

            System.out.println("Iniciando download com: " + String.join(" ", downloadCmd));
            executarComando(downloadCmd, tempFolder);


            File arquivoBaixado = localizarArquivo(nomeBase, tempDir);
            if (arquivoBaixado == null) {
                System.err.println("Erro: Arquivo baixado não encontrado.");
                throw new NullPointerException("");
            }


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
            executarComando(convertCmd, tempFolder);


            File arquivoConvertido = new File(tempFolder, arquivoSaida);
            if (!arquivoConvertido.exists()) {
                throw new IOException("Falha na conversão: arquivo FLAC não gerado.");
            }
            return arquivoConvertido;

        } catch (IOException e) {
            System.err.println("Erro de IO: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processo interrompido.");
        }
        return null;
    }

    private void executarComando(List<String> comando, File workingDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.directory(workingDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();

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
    public void deletarArquivo(File arquivo) {
        if (arquivo != null && arquivo.exists()) {
            boolean apagado = arquivo.delete();
            if (!apagado) {
                System.err.println("Não foi possível deletar " + arquivo.getAbsolutePath());
            }
        }
    }


}
