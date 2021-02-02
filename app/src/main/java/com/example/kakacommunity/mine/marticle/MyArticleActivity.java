package com.example.kakacommunity.mine.marticle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.model.HomeArticle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PrimitiveIterator;

/**
 * 我的帖子
 */
public class MyArticleActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView recyclerView;

    private HomeAdapter myArticleAdapter;

    private List<HomeArticle> myArticleList = new ArrayList<>();

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_article);
        initView();
    }

    private void initView() {
        SharedPreferences preferences = getSharedPreferences("user_message",MODE_PRIVATE);
        userId = preferences.getString("userId","");
        toolbar = (Toolbar)findViewById(R.id.my_article_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = (RecyclerView)findViewById(R.id.my_article_recycler_view);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        myArticleAdapter = new HomeAdapter(myArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(myArticleAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMyArticleJSON();
        myArticleAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onItemCollectClick(int position) {

            }
        });
    }

    private void getMyArticleJSON() {
        for(int i = 0;i < 20;i++) {
            HomeArticle homeArticle = new HomeArticle();
            homeArticle.setAuthor("yzbkaka");
            homeArticle.setTitle("双非的秋招总结-已拿offer");
            homeArticle.setNiceDate(new Date().toString());
            homeArticle.setTag("讨论区");
            myArticleList.add(homeArticle);
        }
    }

    private void parseMyArticleJSON(String responseData) {

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
