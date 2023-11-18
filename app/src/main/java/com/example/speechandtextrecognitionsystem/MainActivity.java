package com.example.speechandtextrecognitionsystem;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private IntentFactory intentFactory; // 注册IntentFactory接口


    class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.SpeechRecognition) {
                Log.d("flag", "进入语音识别系统");
                Intent intent = intentFactory.createSpeechRecognitionIntent();  // 生成Intent时调用工厂方法
                startActivity(intent);
            } else if (v.getId() == R.id.SpeechSynthesis) {
                Log.d("flag", "进入语音合成系统");
                Intent intent = intentFactory.createSpeechSynthesisIntent(); // 生成Intent时调用工厂方法
                startActivity(intent);
            } else if (v.getId() == R.id.PhotographReading) {
                Log.d("flag", "进入拍照识文系统");
                Intent intent = intentFactory.createPhotographReadingIntent(); // 生成Intent时调用工厂方法
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置为全屏模式（隐藏状态条）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button btn1 = findViewById(R.id.SpeechRecognition);
        btn1.setOnClickListener(new BtnClickListener());
        Button btn2 = findViewById(R.id.SpeechSynthesis);
        btn2.setOnClickListener(new BtnClickListener());
        Button btn3 = findViewById(R.id.PhotographReading);
        btn3.setOnClickListener(new BtnClickListener());
        intentFactory = new IntentFactoryImpl(this);
    }

}
