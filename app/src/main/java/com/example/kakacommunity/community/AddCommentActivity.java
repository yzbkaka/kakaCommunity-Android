package com.example.kakacommunity.community;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
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
import static com.example.kakacommunity.constant.kakaCommunityConstant.ENTITY_TYPE_POST;

/**
 * 添加评论
 */
public class AddCommentActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private EditText commentText;

    private FloatingActionButton commentFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        String discussPostId = intent.getStringExtra("discussPostId");
        toolbar = (Toolbar) findViewById(R.id.add_comment_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        commentText = (EditText) findViewById(R.id.add_comment_content);
        commentFinish = (FloatingActionButton) findViewById(R.id.add_comment_finish);
        commentFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(discussPostId);
            }
        });
    }

    /**
     * 添加评论
     */
    private void addComment(String discussPostId) {
        String content = commentText.getText().toString();
        SharedPreferences preferences = getSharedPreferences("user_message", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");
        if (StringUtil.isBlank(content)) {
            Toast.makeText(this, "评论不能为空", Toast.LENGTH_SHORT).show();
        } else {
            RequestBody requestBody = new FormBody.Builder()
                    .add("userId", userId)
                    .add("content", content)
                    .add("entityId", discussPostId)
                    .add("entityType", String.valueOf(ENTITY_TYPE_POST))
                    .build();
            HttpUtil.OkHttpPOST(BASE_ADDRESS + "/comment" + "/add" + "/" + discussPostId,
                    requestBody, new okhttp3.Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseData = response.body().string();
                            if (responseData.contains("成功")) {
                                runOnUiThread(new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AddCommentActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                                Intent intent = new Intent();
                                intent.putExtra("addReply", "refresh");
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:  //默认id
                finish();
                break;
        }
        return true;
    }
}
