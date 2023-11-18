package com.example.speechandtextrecognitionsystem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

class BaiduAIPClient {

    private static BaiduAIPClient instance;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    // 私有化构造函数
    private BaiduAIPClient() {
    }

    // 提供一个公有的静态方法返回实例
    public static BaiduAIPClient getInstance() {
        if (instance == null) {
            // 如果实例不存在，则新建一个实例
            instance = new BaiduAIPClient();
        }
        return instance;
    }

    // 音频转写任务API
    public String createTranscriptionTask(String speechUrl) throws IOException {
        // 从speechUrl中截取后三位作为文件格式(因为每种音频文件类型都是三位的)
        String format = speechUrl.substring(speechUrl.length() - 3);

        MediaType mediaType = MediaType.parse("application/json");
        String requestBodyJson = "{\"speech_url\":\"" + speechUrl + "\",\"format\":\"" + format + "\",\"pid\":80001,\"rate\":16000}";
        RequestBody body = RequestBody.create(mediaType, requestBodyJson);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/aasr/v1/create?access_token="+getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return response.body().string();
    }

    // 查询音频转写任务API
    public String queryTranscriptionTask(String taskId) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"task_ids\":[\"" + taskId + "\"]}");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/aasr/v1/query?access_token="+getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return response.body().string();
    }


    public static final String API_KEY = "Fkx1CM";
    public static final String SECRET_KEY = "CMMxGME";

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    private String getAccessToken() throws IOException {
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