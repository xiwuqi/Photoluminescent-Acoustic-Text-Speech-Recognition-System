package com.example.speechandtextrecognitionsystem;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class BaiduAISSClient {
    private static BaiduAISSClient instance;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    private BaiduAISSClient() {
    }

    // 提供一个公有的静态方法返回实例
    public static BaiduAISSClient getInstance() {
        if (instance == null) {
            // 如果实例不存在，则新建一个实例
            instance = new BaiduAISSClient();
        }
        return instance;
    }

    // 语音合成API
    public byte[] textToSpeech(String text, String per) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String requestBodyString = "tex=" + text +
                "&tok=" + getAccessToken() +
                "&cuid=TbRjfrNukVE7op8dMctMtYNrHlODbW0C" +
                "&ctp=1" +
                "&lan=zh" +
                "&spd=5" +
                "&pit=5" +
                "&vol=5" +
                "&per=" + per +
                "&aue=3";
        RequestBody body = RequestBody.create(mediaType, requestBodyString);
        Request request = new Request.Builder()
                .url("https://tsn.baidu.com/text2audio?")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return response.body().bytes();
    }

    public static final String API_KEY = "FkuxCM";
    public static final String SECRET_KEY = "CMMxME";

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    public String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        try {
            return new JSONObject(response.body().string()).getString("access_token");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}