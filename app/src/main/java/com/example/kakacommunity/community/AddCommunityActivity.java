package com.example.kakacommunity.community;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.example.kakacommunity.R;
import com.example.kakacommunity.utils.HttpUtil;
import com.example.kakacommunity.utils.StringUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;
import static com.example.kakacommunity.mine.MineFragment.userId;

public class AddCommunityActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private EditText addTitle;

    private EditText addContent;

    private FloatingActionButton addFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_community);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.add_community_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        addTitle = (EditText) findViewById(R.id.add_community_title);
        addContent = (EditText) findViewById(R.id.add_community_content);
        addFinish = (FloatingActionButton) findViewById(R.id.add_community_finish);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommunity();
            }
        });
    }

    private void addCommunity() {
        String title = addTitle.getText().toString();
        String content = addContent.getText().toString();
        if (StringUtil.isBlank(title)) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
        } else if (StringUtil.isBlank(content)) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("add","发送");
            RequestBody requestBody = new FormBody.Builder()
                    .add("title", title)
                    .add("content", content)
                    .add("userId",userId)
                    .build();
            HttpUtil.OkHttpPOST(BASE_ADDRESS + "/discuss" + "/add", requestBody, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    Log.e("add",responseData);
                    if (responseData.contains("成功")) {
                        runOnUiThread(new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddCommunityActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                            }
                        }));
                        Intent intent = new Intent();
                        intent.putExtra("add","refresh");
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
            });
        }
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
