package com.example.speechandtextrecognitionsystem;

/**
 * 观察者接口
 */
public interface RecognizeImageTaskObserver {
    void onTaskCompleted(String result);
}