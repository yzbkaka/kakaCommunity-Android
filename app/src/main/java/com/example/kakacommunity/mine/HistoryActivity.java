package com.example.kakacommunity.mine;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.kakacommunity.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar)findViewById(R.id.history_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        tabLayout = (TabLayout)findViewById(R.id.history_tab_layout);
        viewPager = (ViewPager)findViewById(R.id.history_tab_view_pager);
        initViewPager();
    }

    private void initViewPager() {
        List<String> titleList = new ArrayList<>();
        titleList.add("文章");
        titleList.add("项目");
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new HistoryArticleFragment());
        fragmentList.add(new HistoryProjectFragment());
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
