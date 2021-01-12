package com.example.kakacommunity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kakacommunity.utils.HttpUtil;
import com.example.kakacommunity.utils.StringUtil;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextInputLayout loginName;

    private TextInputLayout loginPassword;

    private Button loginButton;

    private TextView loginRegisterText;

    public static String ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        loginName = (TextInputLayout) findViewById(R.id.login_name);
        loginPassword = (TextInputLayout) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);
        loginRegisterText = (TextView) findViewById(R.id.login_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        loginRegisterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String name = loginName.getEditText().getText().toString();
        String password = loginPassword.getEditText().getText().toString();
        if (StringUtil.isBlank(name)) {
            loginName.setErrorEnabled(true);
            loginName.setError("用户名不能为空");
        }
        if (StringUtil.isBlank(password)) {
            loginPassword.setErrorEnabled(true);
            loginPassword.setError("密码不能为空");
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("username", name)
                .add("password", password)
                .add("rememberMe", "true")
                .build();
        HttpUtil.OkHttpPOST(BASE_ADDRESS + "/login", requestBody, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("login", responseData);
                if (responseData.contains("ticket")) {
                    Log.e("login", "请求登录成功");
                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "请求登录成功", Toast.LENGTH_SHORT).show();
                        }
                    }));
                    saveTicket(responseData);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("login", "请求登录失败");
                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "请求登录失败", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }
            }
        });
    }

    /**
     * 存储用户凭证
     */
    private void saveTicket(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            ticket = jsonObject.getString("ticket");
            Log.e("login",ticket);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
