package com.example.kakacommunity.search;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.header.PhoenixHeader;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.mine.collect.CollectTabFragmentAdapter;
import com.example.kakacommunity.mine.history.HistoryArticleFragment;
import com.example.kakacommunity.mine.history.HistoryProjectFragment;
import com.example.kakacommunity.model.HomeArticle;
import com.example.kakacommunity.utils.ActivityUtil;
import com.example.kakacommunity.utils.HttpUtil;
import com.google.android.material.tabs.TabLayout;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;

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
