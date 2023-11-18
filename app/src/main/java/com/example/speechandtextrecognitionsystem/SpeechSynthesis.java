package com.example.speechandtextrecognitionsystem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;


public class SpeechSynthesis extends AppCompatActivity {

    // 定义组件变量
    private ImageButton btnSelectSs;
    private ImageButton btnClearSs;
    private ImageButton btnBeginSs;
    private ImageButton btnCancelSs;
    private EditText etOutputSs;

    // 定义常量或变量
    private static final int FILE_REQUEST_CODE = 1;
    // 创建一个新的 ActivityResultLauncher 用于选择文件
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private String selectedVoice = "度小宇"; // 默认选择度小宇
    private int selectedPer = 1; // 默认对应per为1
    private MediaPlayer mediaPlayer;
    String fileName = null; // 用于记录文件名


    // 初始化函数
    public void init(){
        btnSelectSs = findViewById(R.id.btnSelectSs);
        btnClearSs = findViewById(R.id.btnClearSs);
        btnBeginSs = findViewById(R.id.btnBeginSs);
        btnCancelSs = findViewById(R.id.btnCancelSs);
        etOutputSs = findViewById(R.id.etOutputSs);

        // 注册按键监听器
        BtnClickListener btnClickListener = new BtnClickListener();
        btnSelectSs.setOnClickListener(btnClickListener);
        btnClearSs.setOnClickListener(btnClickListener);
        btnBeginSs.setOnClickListener(btnClickListener);
        btnCancelSs.setOnClickListener(btnClickListener);

        // 初始化用于文件选择的 ActivityResultLauncher
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri uri = data.getData();
                                if (uri != null) {
                                    String content = readTextFromUri(uri);
                                    displayContent(content);
                                }
                            }
                        }
                    }
                });
    }

    //返回按钮
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); //结束Activity
        }
        return super.onOptionsItemSelected(item);
    }

    // 监听器类
    class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnSelectSs) {
                // 点击语音合成选择按钮
                openFilePicker();
            } else if (v.getId() == R.id.btnClearSs) {
                // 点击语音合成清空按钮
                etOutputSs.setText("");
            } else if (v.getId() == R.id.btnBeginSs) {
                // 点击语音合成开始按钮
                Log.d("flag", "点击语音合成开始按钮");
                onBeginSsButtonClick(v);
            } else if (v.getId() == R.id.btnCancelSs) {
                // 点击语音合成取消按钮
                Log.d("flag", "点击语音合成取消按钮");
                // 停止播放
                stopPlayback();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_synthesis);
        Log.d("flag", "SpeechSynthesis onCreate");

        // 设置为全屏模式（隐藏状态条）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("语音合成系统"); //设置标题
        actionBar.setDisplayHomeAsUpEnabled(true); //返回箭头按钮

        init(); // 调用初始化函数
    }

    // 菜单注入
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.ss_menu, menu );
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("flag", "SpeechSynthesis onDestroy");
    }

    /* 具体实现函数----------------------------------------------------------------------------------
    ----------------------------------------------------------------------------------------------*/
    // 用于打开文件
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // 支持任意类型的文件选择
        filePickerLauncher.launch(intent);
    }

    // 从文件流中读取文字信息（前60个字节）
    private String readTextFromUri(Uri uri) {
        StringBuilder content = new StringBuilder();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String scheme = uri.getScheme();
            if (DocumentsContract.Document.MIME_TYPE_DIR.equals(scheme)) {
                // 这是一个目录URI，而不是文件URI，请相应地处理它。
                return "";
            }
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                int totalCharacters = 0;
                int maxCharacters = 60; // 限制为 60 个字节
                int data;
                while ((data = reader.read()) != -1 && totalCharacters < maxCharacters) {
                    content.append((char) data);
                    totalCharacters++;
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "读取文件失败", Toast.LENGTH_SHORT).show();
        }
        return content.toString();
    }

    // 将文件的前 60 字节显示在etOutputSs中
    private void displayContent(String content) {
        etOutputSs.setText(content);
    }

    // 处理btnBeginSs按钮的点击事件
    public void onBeginSsButtonClick(View view) {
        final String[] voices = new String[]{"度小宇", "度小美", "度丫丫", "度逍遥"};
        int checkedItem = 0; // 默认选中第一个（度小宇）

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("选择合成的人声：");
        builder.setSingleChoiceItems(voices, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedVoice = voices[which];
                selectedPer = getPerValue(selectedVoice); // 根据人声选择获取对应的per值
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = etOutputSs.getText().toString(); // 获取etOutputSs中的内容
                // 调用语音合成功能，并传入selectedVoice和selectedPer
                // 例如：synthesizeSpeech(text, selectedPer);
                try {
                    // 将文本转换为完全URL编码形式（两次 URL 编码）
                    text = URLEncoder.encode(URLEncoder.encode(etOutputSs.getText().toString(), "UTF-8"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                // 创建 BaiduAIPClient 类的实例
                BaiduAISSClient aissClient = BaiduAISSClient.getInstance();

                // 调用异步任务进行语音合成请求，并获取固定结果（在 AsyncTask 中执行）
                new SynthesizeSpeechTask().execute(aissClient,text, selectedPer);
            }
        });
        builder.show();
    }

    // 获取人声选择对应的per值
    private int getPerValue(String voice) {
        switch (voice) {
            case "度小美":
                return 0;
            case "度丫丫":
                return 4;
            case "度逍遥":
                return 3;
            default:
                return 1; // 默认选择度小宇对应的per值
        }
    }

    // 用于播放刚生成的音频文件
    private void playAudio(File audioFile) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("flag", "播放音频文件失败");
            Toast.makeText(this, "播放音频文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 停止播放
    private void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 内部类
    // 异步任务用于处理语音合成请求
    private class SynthesizeSpeechTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            String text = (String) objects[1];
            int per = (int) objects[2];
            Log.d("flag", "text: " + text + "\nper: " + per);

            try {
                // 调用 BaiduAISSClient 中的语音合成接口并获取返回的响应结果
                BaiduAISSClient aissClient = (BaiduAISSClient) objects[0];
                byte[] createResponse = aissClient.textToSpeech(text, per + "");
                Log.d("flag", "createResponse: " + createResponse);

                // 获取suffix信息
                String suffix = "mp3"; // 假设默认为mp3格式

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String currentDateAndTime = sdf.format(new Date());

                // 使用当前日期时间作为文件名
                fileName = currentDateAndTime + "." + suffix;

                // 获取音乐文件夹路径
                File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if (!musicDir.exists()) {
                    if (!musicDir.mkdirs()) {
                        Log.d("flag", "创建音乐文件夹失败");
                        return "创建音乐文件夹失败";
                    }
                }

                // 将音频文件保存到音乐文件夹中
                File audioFile = new File(musicDir, fileName);
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(audioFile))) {
                    bos.write(createResponse);
                    Log.d("flag", "音频文件保存到音乐文件夹成功");
                    return "音频文件保存到音乐文件夹成功";
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("flag", "保存音频文件失败");
                    return "保存音频文件失败";
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 处理任务结果，显示在 UI 上
            etOutputSs = findViewById(R.id.etOutputSs);
            if (result.equals("音频文件保存到音乐文件夹成功")) {
                // TODO:播放生成的音乐文件
                // 获取音乐文件路径
                File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                File audioFile = new File(musicDir, fileName);
                playAudio(audioFile);
            } else {
                etOutputSs.setText("语音合成失败");
            }
        }
    }

}