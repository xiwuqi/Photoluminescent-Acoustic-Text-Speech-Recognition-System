package com.example.speechandtextrecognitionsystem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private View progress;

    private View mInputLayout;

    private float mWidth, mHeight;

    private LinearLayout mName, mPsw;
    private EditText etEmailLa;
    private EditText etPswLa;
    private TextView textViewSLLogin;
    private TextView mBtnLogin;
    private LoginState currentState;    // 登录状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // 设置为全屏模式（隐藏状态条）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar(); //获取ActionBar
        actionBar.hide(); //隐藏ActionBar

        initView();
    }

    private void initView() {
        progress = findViewById(R.id.layout_progress);
        mInputLayout = findViewById(R.id.input_layout);
        mName = (LinearLayout) findViewById(R.id.input_layout_name);
        mPsw = (LinearLayout) findViewById(R.id.input_layout_psw);
        etEmailLa = findViewById(R.id.etEmailLa);
        etPswLa = findViewById(R.id.etPswLa);
        textViewSLLogin = findViewById(R.id.textViewSLLogin);
        mBtnLogin = findViewById(R.id.mBtnLogin);
        mBtnLogin.setText("Login");  // 设置初始文本为"Login"
        currentState = new LoginStateImpl(); // 设置初始状态为登录状态

        mBtnLogin.setOnClickListener(this);
        textViewSLLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLoginSignUp();  // 切换登录和注册状态
            }
        });
    }

    @Override
    public void onClick(View v) {
        if ("Login".equals(mBtnLogin.getText().toString())) {
            // 处理登录逻辑
            mWidth = mBtnLogin.getMeasuredWidth();
            mHeight = mBtnLogin.getMeasuredHeight();

            mName.setVisibility(View.INVISIBLE);
            mPsw.setVisibility(View.INVISIBLE);

            inputAnimator(mInputLayout, mWidth, mHeight);
        } else {
            // 处理注册逻辑
            SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
            String username = etEmailLa.getText().toString();
            String password = etPswLa.getText().toString();

            if (sp.contains(username)) {
                Toast.makeText(LoginActivity.this, "用户名已存在", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(username, password);
                editor.apply();
                Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                toggleLoginSignUp();  // 切换登录和注册状态
            }
        }
    }

    // 添加 setState 方法来更新状态
    public void setState(LoginState newState) {
        this.currentState = newState;
    }

    private void toggleLoginSignUp() {
        currentState.toggle(this);
    }

    public void updateUIForLogin() {
        // 更新UI为登录模式
        textViewSLLogin.setText("Sign up");
        mBtnLogin.setText("Login");
    }

    public void updateUIForSignUp() {
        // 更新UI为注册模式
        textViewSLLogin.setText("Login");
        mBtnLogin.setText("Sign up");
    }

    void login() {
        textViewSLLogin.setText("Sign up");
        mBtnLogin.setText("Login");
    }

    void signUp() {
        textViewSLLogin.setText("Login");
        mBtnLogin.setText("Sign up");
    }


    private void inputAnimator(final View view, float w, float h) {



        ValueAnimator animator = ValueAnimator.ofFloat(0, w);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
                params.leftMargin = (int) value;
                params.rightMargin = (int) value;
                view.setLayoutParams(params);
            }
        });
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mInputLayout, "scaleX", 1f, 0.5f);
        set.setDuration(1000);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(animator, animator2);
        set.start();
        set.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                progress.setVisibility(View.VISIBLE);
                progressAnimator(progress);
                mInputLayout.setVisibility(View.INVISIBLE);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        recovery();
                        SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                        String username = etEmailLa.getText().toString();
                        String password = etPswLa.getText().toString();

                        // 检查SharedPreferences是否包含该用户名
                        if (sp.contains(username)) {
                            // 获取存储的密码并进行比较
                            String storedPassword = sp.getString(username, "");
                            if (storedPassword.equals(password)) {
                                // 密码匹配，登录成功
                                Log.d("flag", "进入声文识别系统");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // 密码不匹配
                                Toast.makeText(LoginActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 用户名不存在
                            Toast.makeText(LoginActivity.this, "用户名不存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 2000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });

    }

    private void progressAnimator(final View view) {
        PropertyValuesHolder animator = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f);
        PropertyValuesHolder animator2 = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1f);
        ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(view, animator, animator2);
        animator3.setDuration(1000);
        animator3.setInterpolator(new JellyInterpolator());
        animator3.start();

    }

    /**
     * 恢复初始状态
     */
    private void recovery() {
        progress.setVisibility(View.GONE);
        mInputLayout.setVisibility(View.VISIBLE);
        mName.setVisibility(View.VISIBLE);
        mPsw.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mInputLayout.getLayoutParams();
        params.leftMargin = 0;
        params.rightMargin = 0;
        mInputLayout.setLayoutParams(params);


        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mInputLayout, "scaleX", 0.5f,1f );
        animator2.setDuration(500);
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator2.start();
    }
}