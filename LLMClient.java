package com.yourplugin.sparklineageplugin;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class LLMClient {

    /*
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "YOUR_OPENAI_API_KEY";

    public static String sendPrompt(String prompt) {
        OkHttpClient client = new OkHttpClient();

        String json = "{\"model\":\"gpt-4o\",\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt.replace("\"", "\\\"") + "\"}]}";

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
        return "Error: No response body.";
    }*/


    private static final String API_URL = "http://localhost:11434/api/chat";
    private static final String MODEL_ID = "gemma3:4b";


    public static String sendPrompt(String prompt) {
        System.out.println("Sending prompt:\n" + prompt);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS)
                .readTimeout(1000, TimeUnit.SECONDS)
                .writeTimeout(1000, TimeUnit.SECONDS)
                .build();

        String json = "{\n" +
                "  \"model\": \"" + MODEL_ID + "\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"" + escape(prompt) + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"temperature\": 0.1,\n" +
                "  \"num_predict\": 2048,\n" +
                "  \"top_k\": 50,\n" +
                "  \"top_p\": 0.95,\n" +
                "  \"repeat_penalty\": 1.0,\n" +
                "  \"stream\": false\n" +
                "}";

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Unexpected response: " + response;
            }

            if (response.body() != null) {
                String bodyStr = response.body().string();
                JSONObject obj = new JSONObject(bodyStr);
                return obj.getJSONObject("message").getString("content");
            }
        } catch (IOException e) {
            e.printStackTrace();  // Will show in IDE logs or terminal
            return "LLM Error: " + e.getMessage();
        }

        return "LLM Error: No response received.";
    }

    private static String escape(String prompt) {
        return prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }


  /*  public static void main(String args[])
    {

        JSONObject obj = new JSONObject(sendPrompt("what is the capital of UK?"));
        String content = obj.getJSONObject("message").getString("content");
        return con;

    }*/
  /*  public static String sendPrompt(String prompt) {

        System.out.println(" prompt : \n"+prompt);
        OkHttpClient client = new OkHttpClient();
        String json = "{\n" +
                "  \"model\": \"" + MODEL_ID + "\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"" + escape(prompt) + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"temperature\": 0.1,\n" +
                "  \"num_predict\": 2048,\n" +
                "  \"top_k\": 50,\n" +
                "  \"top_p\": 0.95,\n" +
                "  \"repeat_penalty\": 1.0,\n" +
                "  \"stream\": false\n" +
                "}";
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                JSONObject obj = new JSONObject(extractContent(response.body().string()));
                String content = obj.getJSONObject("message").getString("content");
                return content;
            }
        } catch (IOException e) {

            return "Error here: " + e.getMessage()+" "+e.getCause();
        }

        return "Error: No response received.";
    }

    private static String escape(String prompt) {
        return prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private static String extractContent(String rawJson) {
        // Ollama chat API: {"message":{"role":"assistant","content":"..."}, ...}
        int index = rawJson.indexOf("\"content\":\"");
        if (index != -1) {
            int start = index + 10;
            int end = rawJson.indexOf("\"", start);
            if (end > start) {
                return rawJson.substring(start, end)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
        }
        return rawJson;
    }*/
}
