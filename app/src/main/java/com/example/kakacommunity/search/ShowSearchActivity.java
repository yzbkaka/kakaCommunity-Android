package com.example.kakacommunity.search;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.kakacommunity.R;
import com.example.kakacommunity.mine.collect.CollectTabFragmentAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ShowSearchActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_search);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("keyword");
        toolbar = (Toolbar)findViewById(R.id.show_search_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
        tabLayout = (TabLayout)findViewById(R.id.show_search_tab_layout);
        viewPager = (ViewPager)findViewById(R.id.show_search_tab_view_pager);
        initViewPager();
    }

    private void initViewPager() {
        List<String> titleList = new ArrayList<>();
        titleList.add("文章");
        titleList.add("帖子");
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new ShowSearchArticleFragment());
        fragmentList.add(new ShowSearchCommunityFragment());
        for(int i = 0;i < titleList.size();i++) {
            tabLayout.addTab(tabLayout.newTab().setText(titleList.get(i)));
        }
        CollectTabFragmentAdapter adapter = new CollectTabFragmentAdapter(getSupportFragmentManager(), fragmentList,titleList);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
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
