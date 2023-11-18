package com.example.speechandtextrecognitionsystem;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 创建 PreferencesManager 类,用于获取 SingleInstance 内部类的实例
 */
public class PreferencesManager {

    private SharedPreferences sp1;
    private SharedPreferences sp2;

    private static PreferencesManager instance;

    private PreferencesManager(Context context) {
        sp1 = context.getSharedPreferences("mydata", Context.MODE_PRIVATE);
        sp2 = context.getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    public SharedPreferences getMyDataPreferences() {
        return sp1;
    }

    public SharedPreferences getUserPreferences() {
        return sp2;
    }

}