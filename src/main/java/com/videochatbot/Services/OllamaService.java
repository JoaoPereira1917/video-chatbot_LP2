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
                """
                Você é um assistente virtual especializado em ajudar estudantes a compreender videoaulas.
                Seu objetivo é fornecer explicações didáticas, precisas e úteis, combinando o conteúdo do vídeo com seu conhecimento interno.
                
                Contexto extraído da transcrição do vídeo:
                --- INÍCIO DO CONTEXTO ---
                %s
                --- FIM DO CONTEXTO ---
                
                Com base nesse contexto e no seu conhecimento sobre o assunto, responda à pergunta abaixo.
                
                Pergunta do usuário:
                %s
                
                Diretrizes ESSENCIAIS para sua resposta:
                1. Se o contexto contiver a resposta, use‑o como base principal, citando‑o implicitamente.
                2. Se a pergunta for complementar ao vídeo, expanda com explicações adicionais, relacionando‑as ao tema central.
                3. Ao explicar conceitos técnicos ou de programação, seja extremamente preciso. Evite generalizações incorretas.
                4. Se optar por incluir exemplos de código, eles DEVEM ser sintaticamente corretos e funcionais. Revise mentalmente o código antes de apresentá‑lo.
                5. Caso o contexto não aborde o assunto, informe isso claramente e ofereça uma explicação baseada em fontes confiáveis (seu conhecimento interno), mas sinalize que se trata de um complemento.
                6. Use analogias com moderação e apenas quando realmente ajudarem a clareza. Nunca substitua uma explicação técnica precisa por uma analogia vaga.
                7. Mantenha um tom encorajador, mas sem infantilizar. Lembre‑se de que o usuário está estudando e precisa de informações corretas.
                """
                , contexto, pergunta
        );

        Prompt prompt = new Prompt(promptCompleto);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }


}



