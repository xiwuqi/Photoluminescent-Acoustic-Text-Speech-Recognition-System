package com.example.speechandtextrecognitionsystem;

import android.os.AsyncTask;

import java.io.IOException;

/**
 * 实现异步调用OCR接口
 */
public class RecognizeImageTask extends AsyncTask<Object, Void, String> {
    private RecognizeImageTaskObserver observer;

    public RecognizeImageTask(RecognizeImageTaskObserver observer) {
        this.observer = observer;
    }

    @Override
    protected String doInBackground(Object... objects) {
        String result = (String)objects[0];

        try {
            // 创建 BaiduAIPClient 类的实例
            BaiduOCRClient ocrClient = BaiduOCRClient.getInstance();
            return ocrClient.ocrGeneral(result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (observer != null) {
            observer.onTaskCompleted(result);
        }
    }
}
