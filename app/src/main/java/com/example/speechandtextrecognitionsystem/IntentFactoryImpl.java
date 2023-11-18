package com.example.speechandtextrecognitionsystem;

import android.content.Context;
import android.content.Intent;

/**
 * 创建具体工厂类,实现抽象方法
 */
public class IntentFactoryImpl implements IntentFactory {

    private final Context context;  // 可以解决静态内部类访问外部类问题

    public IntentFactoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public Intent createSpeechRecognitionIntent() {
        return new Intent(context, SpeechRecognition.class);
    }

    @Override
    public Intent createSpeechSynthesisIntent() {
        return new Intent(context, SpeechSynthesis.class);
    }

    @Override
    public Intent createPhotographReadingIntent() {
        return new Intent(context, PhotographReading.class);
    }

}
