package com.example.speechandtextrecognitionsystem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import okhttp3.*;

public class BaiduOCRClient {
    private static BaiduOCRClient instance;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    private BaiduOCRClient() {
    }

    // 提供一个公有的静态方法返回实例
    public static BaiduOCRClient getInstance() {
        if (instance == null) {
            // 如果实例不存在，则新建一个实例
            instance = new BaiduOCRClient();
        }
        return instance;
    }

    public String ocrGeneral(String imageBase64) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String requestBodyString = "image=" + imageBase64 + "&language_type=CHN_ENG";
        RequestBody body = RequestBody.create(mediaType, requestBodyString);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/ocr/v1/general?access_token="+getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return response.body().string();
    }

    public static final String API_KEY = "FkxM";
    public static final String SECRET_KEY = "CMMxGME";

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