package com.videochatbot.Services;

import com.videochatbot.Interfaces.ChatServiceInterface;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaService implements ChatServiceInterface {

    private final OllamaChatModel chatModel;

    public OllamaService(OllamaChatModel chatModel){
        this.chatModel = chatModel;
    }

    public String perguntar(String pergunta, String contexto) {
        String promptCompleto = String.format(
                "Você é um assistente que responde dúvidas baseado APENAS no seguinte contexto:\n\n" +
                        "--- CONTEXTO (transcrição da aula) ---\n%s\n" +
                        "--- FIM DO CONTEXTO ---\n\n" +
                        "Com base SOMENTE no contexto acima, responda: %s\n\n" +
                        "Se a pergunta não puder ser respondida com o contexto, diga 'Não encontrei essa informação no vídeo.'",
                contexto, pergunta
        );

        Prompt prompt = new Prompt(promptCompleto);
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }


}



