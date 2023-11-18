package com.example.speechandtextrecognitionsystem;

import android.content.Intent;

/**
 * 创建抽象工厂接口
 */
public interface IntentFactory {

    Intent createSpeechRecognitionIntent();

    Intent createSpeechSynthesisIntent();

    Intent createPhotographReadingIntent();

}