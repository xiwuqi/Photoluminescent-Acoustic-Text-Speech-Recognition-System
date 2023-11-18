package com.example.speechandtextrecognitionsystem;

// 注册状态实现
public class SignUpStateImpl implements LoginState {
    @Override
    public void toggle(LoginActivity context) {
        context.setState(new LoginStateImpl());
        context.updateUIForLogin();
    }

    @Override
    public void performAction(LoginActivity context) {
        context.signUp();
    }
}