package com.example.kakacommunity.community;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.model.CommunityReply;
import com.example.kakacommunity.model.CommunityComment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private CircleImageView commentImage;

    private TextView commentName;

    private TextView commentTime;

    private TextView commentContent;

    private RecyclerView recyclerView;

    private FloatingActionButton addReply;

    private ReplyDetailAdapter adapter;

    private List<CommunityReply> commentReplyList = new ArrayList<>();

    private String commentId;

    public static final int REPLY_DETAIL_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_detail);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        CommunityComment communityReply = (CommunityComment) intent.getSerializableExtra("communityReply");
        commentId = communityReply.getId();
        toolbar = (Toolbar) findViewById(R.id.reply_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        commentImage = (CircleImageView) findViewById(R.id.reply_detail_author_image);
        commentName = (TextView) findViewById(R.id.reply_detail_author);
        commentTime = (TextView) findViewById(R.id.reply_detail_time);
        commentContent = (TextView) findViewById(R.id.reply_detail_content);
        recyclerView = (RecyclerView) findViewById(R.id.reply_detail_recycler_view);
        addReply = (FloatingActionButton) findViewById(R.id.reply_detail_floating_actionbar);
        addReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReplyDetailActivity.this, AddReplyActivity.class);
                intent.putExtra("commentId", commentId);
                startActivityForResult(intent,REPLY_DETAIL_CODE);
            }
        });
        setView(communityReply);
    }

    /**
     * 将数据进行显示
     */
    private void setView(CommunityComment communityReply) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REPLY_DETAIL_CODE:
                Intent intent = new Intent();
                intent.putExtra("addReply", "refresh");
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
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
