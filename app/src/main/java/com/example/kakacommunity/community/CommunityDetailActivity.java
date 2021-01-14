package com.example.kakacommunity.community;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.header.PhoenixHeader;
import com.example.kakacommunity.model.CommuityReply;
import com.example.kakacommunity.utils.ActivityUtil;
import com.example.kakacommunity.utils.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.community.CommunityFragment.COMMUNITY_FRAGMENT_CODE;
import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;

public class CommunityDetailActivity extends AppCompatActivity {

    private RefreshLayout refreshLayout;

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

    private FloatingActionButton addReply;

    private RecyclerView recyclerView;

    private CommunityDetailAdapter adapter;

    private List<CommuityReply> communityReplyList = new ArrayList<>();

    private String discussPostId;

    public static final int COMMUNITY_REPLY_CODE = 10;

    private int curPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);
        initView();
    }


    private void initView() {
        Intent intent = getIntent();
        discussPostId = intent.getStringExtra("discussPostId");
        refreshLayout = (RefreshLayout)findViewById(R.id.community_detail_refresh_layout);
        initRefreshView();
        toolbar = (Toolbar) findViewById(R.id.community_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        communityTitle = (TextView) findViewById(R.id.community_detail_title);
        authorImage = (CircleImageView) findViewById(R.id.community_detail_author_image);
        authorName = (TextView) findViewById(R.id.community_detail_author);
        communityTime = (TextView) findViewById(R.id.community_detail_time);
        communityContent = (TextView) findViewById(R.id.community_detail_content);
        addReply = (FloatingActionButton) findViewById(R.id.community_detail_floating_actionbar);
        addReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(CommunityDetailActivity.this,AddReplyActivity.class);
                intent1.putExtra("discussPostId",discussPostId);
                startActivityForResult(intent1,COMMUNITY_REPLY_CODE);
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.community_detail_recycler_view);
        initRecyclerView();
        getDetailJSON(1);
    }

    private void initRefreshView() {
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new PhoenixHeader(MyApplication.getContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                curPage++;
                getDetailJSON(curPage);
                refreshLayout.finishLoadMore();
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        adapter = new CommunityDetailAdapter(communityReplyList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void getDetailJSON(int page) {
        HttpUtil.OkHttpGET(BASE_ADDRESS + "/discuss" + "/detail" + "/" + discussPostId + "/" + page, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseDetailJSON(responseData);
                Log.e("communityDetail",responseData);
                if(!ActivityUtil.isDestroy(CommunityDetailActivity.this)) {
                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    }));
                }
            }
        });
    }

    private void parseDetailJSON(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject discussPost = jsonObject.getJSONObject("discussPost");  //解析帖子标题&内容
            title = discussPost.getString("title");
            content = discussPost.getString("content");
            createTime = discussPost.getString("createTime");
            JSONObject user = jsonObject.getJSONObject("user");  //解析帖子作者
            username = user.getString("username");
            headerUrl = user.getString("headerUrl");
            JSONArray comments = jsonObject.getJSONArray("comments");  //解析帖子回复
            for(int i = 0;i < comments.length();i++) {
                CommuityReply commuityReply = new CommuityReply();
                JSONObject item = comments.getJSONObject(i);
                JSONObject comment = item.getJSONObject("comment");
                commuityReply.setContent(comment.getString("content"));
                commuityReply.setTime(comment.getString("createTime"));
                JSONObject replyUser = item.getJSONObject("user");
                commuityReply.setName(replyUser.getString("username"));
                commuityReply.setImageUrl(replyUser.getString("headerUrl"));
                communityReplyList.add(commuityReply);
            }
        } catch (Exception e) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case COMMUNITY_FRAGMENT_CODE:
                if (resultCode == RESULT_OK) {
                    communityReplyList.clear();
                    getDetailJSON(1);
                }
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
