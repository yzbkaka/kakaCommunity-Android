package com.example.kakacommunity.community;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.model.CommuityReply;
import com.example.kakacommunity.utils.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;

public class CommunityDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView communityTitle;

    private String title;

    private CircleImageView authorImage;

    private String headerUrl;

    private TextView authorName;

    private String username;

    private TextView communityTime;

    private String createTime;

    private TextView communityContent;

    private String content;

    private FloatingActionButton floatingActionButton;

    private RecyclerView recyclerView;

    private CommunityDetailAdapter adapter;

    private List<CommuityReply> communityReplyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);
        initView();
    }


    private void initView() {
        Intent intent = getIntent();
        String discussPostId = intent.getStringExtra("discussPostId");
        toolbar = (Toolbar)findViewById(R.id.community_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        communityTitle = (TextView)findViewById(R.id.community_detail_title);
        authorImage = (CircleImageView) findViewById(R.id.community_detail_author_image);
        authorName = (TextView)findViewById(R.id.community_detail_author);
        communityTime = (TextView)findViewById(R.id.community_detail_time);
        communityContent = (TextView)findViewById(R.id.community_detail_content);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.community_detail_floating_actionbar);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        recyclerView = (RecyclerView)findViewById(R.id.community_detail_recycler_view);
        initRecyclerView();
        getDetailJSON(discussPostId);
        getReplyJSON();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new CommunityDetailAdapter(communityReplyList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void getDetailJSON(String discussPostId) {
        try {
            HttpUtil.OkHttpGET(BASE_ADDRESS + "/discuss" + "/detail" + "/" + discussPostId, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    parseDetailJSON(responseData);
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDetailJSON(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject discussPost = jsonObject.getJSONObject("discussPost");
            title = discussPost.getString("title");
            content = discussPost.getString("content");
            createTime = discussPost.getString("createTime");
            JSONObject user = jsonObject.getJSONObject("user");
            username = user.getString("username");
            headerUrl = user.getString("headerUrl");
        }catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                communityTitle.setText(title);
                communityContent.setText(content);
                communityTime.setText(createTime);
                authorName.setText(username);
                Glide.with(MyApplication.getContext())
                        .load(headerUrl)
                        .into(authorImage);
            }
        }));
    }

    private void getReplyJSON() {
        for(int i = 0;i < 15;i++) {
            CommuityReply commuityReply = new CommuityReply();
            commuityReply.setName("yzbkaka");
            commuityReply.setTime(String.valueOf(new Date()));
            commuityReply.setContent("声音小的话，在视频界面右键选择视频音效，罢那个默认在中间的，调到最大，然后选择清澈人声，声音贼大，我也是昨天才发现的");
            communityReplyList.add(commuityReply);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:  //默认id
                finish();
                break;
        }
        return true;
    }
}
