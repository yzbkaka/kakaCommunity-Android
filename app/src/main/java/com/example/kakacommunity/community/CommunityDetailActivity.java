package com.example.kakacommunity.community;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.model.CommuityReply;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommunityDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView communityTitle;

    private ImageView authorImage;

    private TextView authorName;

    private TextView communityTime;

    private TextView communityContent;

    private FloatingActionButton floatingActionButton;

    private RecyclerView recyclerView;

    private CommunityDetailAdapter adapter;

    private List<CommuityReply> commuityReplyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar)findViewById(R.id.community_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        communityTitle = (TextView)findViewById(R.id.community_detail_title);
        authorImage = (ImageView)findViewById(R.id.community_detail_author_image);
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

        getReplyJSON();
    }


    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new CommunityDetailAdapter(commuityReplyList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void getReplyJSON() {
        for(int i = 0;i < 15;i++) {
            CommuityReply commuityReply = new CommuityReply();
            commuityReply.setName("yzbkaka");
            commuityReply.setTime(String.valueOf(new Date()));
            commuityReply.setContent("声音小的话，在视频界面右键选择视频音效，罢那个默认在中间的，调到最大，然后选择清澈人声，声音贼大，我也是昨天才发现的");
            commuityReplyList.add(commuityReply);
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
