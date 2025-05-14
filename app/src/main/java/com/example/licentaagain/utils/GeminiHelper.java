package com.example.licentaagain.utils;

import android.content.Context;
import com.example.licentaagain.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

public class GeminiHelper {
    public void getResponse(Context context, String query, ResponseCallback callback) {
        GenerativeModelFutures model = getModel(context);

        Content content = new Content.Builder().addText(query).build();
        Executor executor = Runnable::run;

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                callback.onResponse(resultText);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }
        }, executor);

    }
    private GenerativeModelFutures getModel(Context context) {
        String apiKey = context.getResources().getString(R.string.gemini_ai_api_key);

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.4f;
        configBuilder.topK = 16;
        configBuilder.topP = 0.1f;
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = new GenerativeModel(
                "models/gemini-1.5-pro",
                apiKey,
                generationConfig
        );

        return GenerativeModelFutures.from(gm);
    }

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(Throwable throwable);
    }
}