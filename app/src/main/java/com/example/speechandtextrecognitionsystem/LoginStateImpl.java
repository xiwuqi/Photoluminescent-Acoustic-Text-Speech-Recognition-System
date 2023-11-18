package com.example.speechandtextrecognitionsystem;

/**
 * 登录状态实现
 */
public class LoginStateImpl implements LoginState {
    @Override
    public void toggle(LoginActivity context) {
        context.setState(new SignUpStateImpl());
        context.updateUIForSignUp();
    }

    @Override
    public void performAction(LoginActivity context) {
        context.login();
    }
}