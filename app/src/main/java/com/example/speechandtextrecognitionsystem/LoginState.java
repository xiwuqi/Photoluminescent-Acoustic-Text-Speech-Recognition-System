package com.example.speechandtextrecognitionsystem;

/**
 * 状态模式-状态接口
 */
public interface LoginState {
    void toggle(LoginActivity context);
    void performAction(LoginActivity context);
}
