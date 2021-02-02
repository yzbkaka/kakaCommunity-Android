package com.example.kakacommunity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.kakacommunity.utils.HttpUtil;
import com.example.kakacommunity.utils.LogUtil;
import com.example.kakacommunity.utils.StringUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;

/**
 * 注册功能
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Toolbar toolbar;

    private TextInputLayout registerName;

    private TextInputLayout registerPassword;

    private TextInputLayout registerEmail;

    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        registerName = (TextInputLayout) findViewById(R.id.register_name);
        registerPassword = (TextInputLayout) findViewById(R.id.register_password);
        registerEmail = (TextInputLayout) findViewById(R.id.register_email);
        registerButton = (Button) findViewById(R.id.register_button);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String name = registerName.getEditText().getText().toString();
        String password = registerPassword.getEditText().getText().toString();
        String email = registerEmail.getEditText().getText().toString();
        if (StringUtil.isBlank(name)) {
            registerName.setErrorEnabled(true);
            registerName.setError("用户名不能为空");
        }
        if (StringUtil.isBlank(password)) {
            registerPassword.setErrorEnabled(true);
            registerPassword.setError("密码不能为空");
        }
        if (!StringUtil.isEmail(email)) {
            registerEmail.setErrorEnabled(true);
            registerEmail.setError("邮箱错误");
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("username", name)
                .add("password", password)
                .add("email", email)
                .build();
        HttpUtil.OkHttpPOST(BASE_ADDRESS + "/register", requestBody, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                LogUtil.d(TAG,responseData);
                if (responseData.contains("成功")) {
                    LogUtil.d(TAG,"注册成功");
                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        }
                    }));
                    finish();
                } else {
                    LogUtil.d(TAG,"注册失败");
                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (responseData.contains("该账号已存在")) {
                                registerName.setErrorEnabled(true);
                                registerName.setError("该账号已存在");
                            }
                            if (responseData.contains("该邮箱已经注册")) {
                                registerEmail.setErrorEnabled(true);
                                registerEmail.setError("该邮箱已经注册");
                            }
                        }
                    }));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }
}
