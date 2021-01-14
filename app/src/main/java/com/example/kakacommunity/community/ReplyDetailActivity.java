package com.example.kakacommunity.community;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.model.CommentReply;
import com.example.kakacommunity.model.CommuityReply;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private CircleImageView commentImage;

    private String headerUrl;

    private TextView commentName;

    private String username;

    private TextView commentTime;

    private String createTime;

    private TextView commentContent;

    private String content;

    private RecyclerView recyclerView;

    private ReplyDetailAdapter adapter;

    private List<CommentReply> commentReplyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_detail);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar)findViewById(R.id.reply_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        commentImage = (CircleImageView)findViewById(R.id.reply_detail_author_image);
        commentName = (TextView)findViewById(R.id.reply_detail_author);
        commentTime = (TextView)findViewById(R.id.reply_detail_time);
        commentContent = (TextView)findViewById(R.id.reply_detail_content);
        recyclerView = (RecyclerView)findViewById(R.id.reply_detail_recycler_view);
        //initRecyclerView();
        Intent intent = getIntent();
        CommuityReply communityReply = (CommuityReply) intent.getSerializableExtra("communityReply");
        setView(communityReply);
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new ReplyDetailAdapter(commentReplyList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 将数据进行显示
     */
    private void setView(CommuityReply communityReply) {
        Glide.with(MyApplication.getContext())
                .load(communityReply.getImageUrl())
                .into(commentImage);
        commentName.setText(communityReply.getName());
        commentTime.setText(communityReply.getTime());
        commentContent.setText(communityReply.getContent());
        commentReplyList = communityReply.getCommentReplyList();
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new ReplyDetailAdapter(commentReplyList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
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
