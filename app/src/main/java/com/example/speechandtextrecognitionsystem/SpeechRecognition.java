package com.example.speechandtextrecognitionsystem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpeechRecognition extends AppCompatActivity {

    // 定义组件变量
    private ImageButton btnSelectSr;
    private ImageButton btnClearSr;
    private ImageButton btnRecordSr;
    private ImageButton btnCopySr;
    private EditText etOutputSr;

    // 定义常量和变量
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    ArrayAdapter<String> adapter;
    List<String> suggestions;
    private String taskId;

    // 初始化函数
    @SuppressLint("MissingInflatedId")
    public void init(){
        btnSelectSr = findViewById(R.id.btnSelectSr);
        btnClearSr = findViewById(R.id.btnClearSr);
        btnRecordSr = findViewById(R.id.btnRecordSr);
        btnCopySr = findViewById(R.id.btnCopySr);
        etOutputSr = findViewById(R.id.etOutputSr);
        taskId = "0"; // 表明还未创建过任务


        // 读取 SharedPreferences 中的 key
        SharedPreferences sp = getSharedPreferences("mydata", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sp.getAll();
        suggestions = new ArrayList<>(allEntries.keySet());
        // 初始化 ArrayAdapter
        adapter = new ArrayAdapter<>(SpeechRecognition.this,
                android.R.layout.simple_dropdown_item_1line, suggestions);


        // 注册按键监听器
        BtnClickListener btnClickListener = new BtnClickListener();
        btnSelectSr.setOnClickListener(btnClickListener);
        btnClearSr.setOnClickListener(btnClickListener);
        btnRecordSr.setOnClickListener(btnClickListener);
        btnCopySr.setOnClickListener(btnClickListener);


        // 注册 ActivityResultLauncher 来处理文件选择结果
        filePickerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                // 处理选定的WAV文件
                                Uri selectedFileUri = result.getData().getData();
                                // TODO:使用所选文件进行进一步处理
                            }
                        });

        // 注册 ActivityResultLauncher 用于语音识别
        speechRecognitionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        List<String> results = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (results != null && !results.isEmpty()) {
                            String spokenText = results.get(0);
                            // Do something with the recognized speech text, e.g., display it in the output EditText
                            EditText etOutputSr = findViewById(R.id.etOutputSr);
                            etOutputSr.setText(spokenText);
                        }
                    }
                });
    }

    // 菜单按钮监听
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }else if(item.getItemId() == R.id.srItemRefresh){
            if(taskId.equals("0")){
                Toast.makeText(this, "还未有转换任务创建哦(⊙o⊙)!", Toast.LENGTH_SHORT).show();
            }else{
                // 获取 BaiduAIPClient 类的单例实例
                BaiduAIPClient aipClient = BaiduAIPClient.getInstance();
                // 调用 BaiduAIPClient 的查询方法，并获取固定结果（在 AsyncTask 中执行）
                new QueryTranscriptionTask().execute(aipClient);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 监听器类
    class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnSelectSr) {
                Log.d("flag", "点击语言识别选择按钮");
//                Toast.makeText(SpeechRecognition.this, "音频文件已处理完成", Toast.LENGTH_LONG).show();
//                 处理选择按钮的点击事件：跳转选择wav文件
                showInputDialog();

            } else if (v.getId() == R.id.btnClearSr) {
                Log.d("flag", "点击语言识别清空按钮");
                // 处理清空按钮的点击事件：清空etOutputSr文本框内的识别结果
                clearRecognitionResult();
            } else if (v.getId() == R.id.btnRecordSr) {
                Log.d("flag", "点击语言识别录音按钮");
                // 处理手柄录音按钮的点击实践:开始语音识别
                startSpeechRecognition();
            } else if (v.getId() == R.id.btnCopySr) {
                Log.d("flag", "点击语言识别复制按钮");
                // 处理复制按钮点击实践:复制文本到剪贴板
                copyRecognitionResultToClipboard();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);
        Log.d("flag", "SpeechRecognition onCreate");

        // 设置为全屏模式（隐藏状态条）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("语音识别系统"); //设置标题
        actionBar.setDisplayHomeAsUpEnabled(true); //返回箭头按钮

        init(); // 调用初始化函数

    }

    // 菜单注入
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.sr_menu, menu );
        return super.onCreateOptionsMenu(menu);
    }


    /* 具体实现函数----------------------------------------------------------------------------------
    ----------------------------------------------------------------------------------------------*/


    // 清空文本框
    private void clearRecognitionResult() {
        etOutputSr.setText(""); // 清空etOutputSr文本框内的内容
    }

    // 语言识别
    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        try {
            speechRecognitionLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            // 处理语音识别活动在设备上不可用的情况
        }
    }

    // 结果复制到粘贴板
    private void copyRecognitionResultToClipboard() {
        String recognizedText = etOutputSr.getText().toString();

        // 将识别的文本复制到剪贴板
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("RecognizedText", recognizedText);
        clipboard.setPrimaryClip(clip);
    }

    // 显示输入对话框并处理数据
    private void showInputDialog() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);

        // 创建弹窗布局视图
        final View dialogView = LayoutInflater.from(SpeechRecognition.this).inflate(R.layout.dialog_input, null);
        // 找到 AutoCompleteTextView
        final AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autocompleteInput);
        autoCompleteTextView.setAdapter(adapter);
        String inputText;

        // 创建 AlertDialog
        new AlertDialog.Builder(SpeechRecognition.this)
                .setTitle("直链地址输入弹窗")
                .setView(dialogView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 处理确定按钮点击事件
                        String inputText = autoCompleteTextView.getText().toString();

                        // 验证输入是否合法
                        if (isValidInputText(inputText)) {
                            // TODO: 在这里进行输入框数据的处理
                            if (!suggestions.contains(inputText)) {
                                suggestions.add(inputText);
                                adapter.add(inputText);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(SpeechRecognition.this, "正在处理音频文件:" + inputText, Toast.LENGTH_SHORT).show();
                            }

                            // 获取 BaiduAIPClient 类的单例实例
                            BaiduAIPClient aipClient = BaiduAIPClient.getInstance();

                            // 调用 BaiduAIPClient 的查询方法，并获取固定结果（在 AsyncTask 中执行）
                            new QueryTranscriptionTask().execute(aipClient, inputText);
                        } else {
                            // 输入不合法，显示Toast提示
                            Toast.makeText(SpeechRecognition.this, "请输入合法的音频直链地址（以http://或https://开头，并以.pcm、.wav、.amr、.mp3或.m4a结尾）", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private boolean isValidInputText(String inputText) {
        // 判断输入是否以http://或https://开头，并以.pcm、.wav、.amr、.mp3或.m4a结尾
        return inputText.matches("^(http|https)://.*\\.(pcm|wav|amr|mp3|m4a)$");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("flag", "SpeechRecognition onDestroy");
    }

    // 内部类
    // AsyncTask 用于执行查询任务
    private class QueryTranscriptionTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // 根据参数个数来决定执行不同的逻辑
            if (objects.length == 1) {
                // 仅查询任务
                BaiduAIPClient aipClient = (BaiduAIPClient) objects[0];
                Log.d("flag", "taskId: "+taskId);
                return queryTask(aipClient,taskId);
            } else if (objects.length == 2) {
                // 创建任务并查询
                BaiduAIPClient aipClient = (BaiduAIPClient) objects[0];
                String speechUrl = (String) objects[1];
                return createAndQueryTask(aipClient,speechUrl);
            } else {
                // 参数错误，返回null或抛出异常
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 处理 api 响应的 json 结果，并显示在 UI 上
            EditText etOutputSr = findViewById(R.id.etOutputSr);
            try {
                Log.d("flag", result);
                JSONObject jsonObject = new JSONObject(result);
                JSONArray tasksInfo = jsonObject.getJSONArray("tasks_info");
                Log.d("flag", tasksInfo.toString());

                if (tasksInfo.length() > 0) {
                    String taskStatus = tasksInfo.getJSONObject(0).getString("task_status");
                    Log.d("flag", ""+taskStatus);
                    if(taskStatus.toString().equals("Success")){
                        JSONObject taskResult = tasksInfo.getJSONObject(0).getJSONObject("task_result");
                        JSONArray resultArray = taskResult.getJSONArray("result");
                        // 获取 "result" 部分的内容
                        String resultText = resultArray.getString(0);
                        // 这里将 resultText 显示在你的 UI 上
                        etOutputSr.setText(resultText);
                        Log.d("flag", "转换处理完成，处理结果为："+ resultText);
                    }else if(taskStatus.toString().equals("Running")){
                        etOutputSr.setText("转换任务正在进行中...\n请刷新试试ヾ(≧▽≦*)o");
                        Log.d("flag", "转换任务正在进行中...");
                    }else{
                        Log.d("flag", "接口调试错误");
                    }
                } else {
                    etOutputSr.setText("No result found.");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 创建任务并查询的逻辑
        private String createAndQueryTask(BaiduAIPClient aipClient,String speechUrl) {
            String flag = readSP(speechUrl);
            try {
                // 步骤1：创建音频转写任务并从响应中获取taskId
                if("null".equals(flag)){
                    String createResponse = aipClient.createTranscriptionTask(speechUrl);
                    JSONObject createJsonResponse = new JSONObject(createResponse);
                    taskId = createJsonResponse.getString("task_id");
                    Log.d("flag", "task_id:" + taskId);
                    saveData(speechUrl,taskId); // 保存键值对
                }else{
//                    Toast.makeText(SpeechRecognition.this,"当前音频任务已经创建过啦(^∇^*)",Toast.LENGTH_SHORT);
                    Log.d("flag", "当前音频任务已经创建过啦(^∇^*)");
                }

                // 步骤2：使用taskId查询音频转写任务
                String queryResponse = aipClient.queryTranscriptionTask(flag);
                Log.d("flag", "now" + queryResponse);
                return queryResponse;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        // 仅查询任务的逻辑
        private String queryTask(BaiduAIPClient aipClient,String taskId) {
            try {
                // 使用taskId查询音频转写任务
                String queryResponse = aipClient.queryTranscriptionTask(taskId);
                Log.d("flag", "now" + queryResponse);
                return queryResponse;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //保存key-value数据
        public void saveData(String speechUrl,String taskId) {
            SharedPreferences sp = getSharedPreferences("mydata", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(speechUrl, taskId);
            editor.apply();
            Log.d("flag", "数据保存成功\n"+speechUrl+"-"+taskId);
        }

        //读取数据
        public String readSP(String speechUrl) {
            SharedPreferences sp = getSharedPreferences("mydata", Context.MODE_PRIVATE);
            String flag;
            flag =  sp.getString(speechUrl,"null");
            Log.d("flag", "readSP: "+flag);
            return flag;
        }
    }

}