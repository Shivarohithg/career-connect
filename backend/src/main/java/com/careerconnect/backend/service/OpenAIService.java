package com.careerconnect.backend.service;

import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

@Service
public class OpenAIService {

    private final OpenAIClient client;

    public OpenAIService() {

        client = OpenAIOkHttpClient.fromEnv();

    }

    public String generateCareerAdvice(String prompt) {

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .input(prompt)
                        .model(ChatModel.GPT_5_2)
                        .build();

        Response response =
                client.responses().create(params);

        return response.output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .findFirst()
                .orElse("No response generated.");
    }
}