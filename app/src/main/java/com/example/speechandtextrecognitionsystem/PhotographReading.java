package com.example.speechandtextrecognitionsystem;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class PhotographReading extends AppCompatActivity implements RecognizeImageTaskObserver {
    private static final int REQUEST_IMAGE_PICK = 1;

    private Button btnSelectImagePr;
    private Button btnClearImagePr;
    private ImageView imgInput;
    private EditText etRecognitionResultPr;
    private ScrollView scrollView;

    ActivityResultLauncher launcher2;



    // 初始化函数
    public void init(){
        // 注册按键
        btnSelectImagePr = findViewById(R.id.btnSelectImagePr);
        btnClearImagePr = findViewById(R.id.btnClearImagePr);
        imgInput = findViewById(R.id.imgInput);
        etRecognitionResultPr = findViewById(R.id.etRecognitionResultPr);
        scrollView = findViewById(R.id.scrollView);

        // 注册按键的监听器
        btnSelectImagePr.setOnClickListener(new BtnClickListener());
        btnClearImagePr.setOnClickListener(new BtnClickListener());

        // 创建启动器
        launcher2 = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        // 获取选择的图片Uri
                        Uri imageUri = result;
                        try {
                            // 将选择的图片设置到ImageView中
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            imgInput.setImageBitmap(bitmap);
                            // 显示ImageView，隐藏按钮
                            imgInput.setVisibility(View.VISIBLE);
                            btnClearImagePr.setVisibility(View.VISIBLE);
                            btnSelectImagePr.setVisibility(View.GONE);

                            // 清空识别结果文本框
                            etRecognitionResultPr.setText("");

                            // 获取图像数据的字节数组
                            byte[] imageBytes = getImageBytesFromUri(imageUri);
                            String resulttxt = bitmapToBase64(imageBytes);
                            // 调用 RecognizeImageTask 任务进行OCR识别
                            new RecognizeImageTask(PhotographReading.this).execute(resulttxt);
                        } catch (IOException e) {
                            e.printStackTrace();
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

    @Override
    public void onTaskCompleted(String result) {
        // 之前在 onPostExecute 方法中的逻辑
        if (result != null) {
            // 解析OCR识别结果
            JSONObject jsonResponse;
            JSONArray wordsResultArray;
            try {
                jsonResponse = new JSONObject(result);
                wordsResultArray = jsonResponse.getJSONArray("words_result");

                // 将多个识别结果合并成一条完整的文字
                StringBuilder mergedText = new StringBuilder();
                for (int i = 0; i < wordsResultArray.length(); i++) {
                    JSONObject wordsResult = wordsResultArray.getJSONObject(i);
                    String words = wordsResult.getString("words");
                    mergedText.append(words).append("\n"); // 每个识别结果后面加上换行符

                }
                // 显示合并后的识别结果
                etRecognitionResultPr.setText(mergedText.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            etRecognitionResultPr.setText("OCR识别失败");
        }
    }

    class BtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnSelectImagePr) {
                Log.d("flag", "选择图片");
                // 获取所有本地图片，选择图片
                launcher2.launch("image/*");

            } else if (v.getId() == R.id.btnClearImagePr) {
                // 清除图片，并显示选择图片按钮
                imgInput.setImageBitmap(null);
                imgInput.setVisibility(View.GONE);
                btnClearImagePr.setVisibility(View.GONE);
                btnSelectImagePr.setVisibility(View.VISIBLE);

                // 清空识别结果文本框
                etRecognitionResultPr.setText("");
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photograph_reading);
        Log.d("flag", "PhotographReading onCreate");

        // 设置为全屏模式（隐藏状态条）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("拍照识文系统"); //设置标题
        actionBar.setDisplayHomeAsUpEnabled(true); //返回箭头按钮

        init();
    }

    // 回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("flag", "PhotographReading onDestroy");
    }

    // 辅助方法：将Bitmap转换为Base64编码字符串,在进行URL编码
    private String bitmapToBase64(byte[] b) throws UnsupportedEncodingException {
        String base64 = Base64.encodeToString(b,1);
        base64 = URLEncoder.encode(base64, "utf-8");
        return base64;
    }


    // 将图像数据转换为字节数组
    public byte[] getImageBytesFromUri(Uri imageUri) throws IOException {
        // 从图像Uri获取InputStream
        InputStream inputStream = getContentResolver().openInputStream(imageUri);

        // 从InputStream读取图像数据
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        byteArrayOutputStream.close();
        inputStream.close();

        // 将图像数据转换为字节数组
        return byteArrayOutputStream.toByteArray();
    }

}