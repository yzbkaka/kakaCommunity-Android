package com.example.kakacommunity.mine.tree;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;

import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.header.PhoenixHeader;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.HomeArticle;
import com.example.kakacommunity.utils.ActivityUtil;
import com.example.kakacommunity.utils.HttpUtil;
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
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;

/**
 * 知识体系展示
 */
public class TreeArticleActivity extends AppCompatActivity {

    private MyDataBaseHelper dataBaseHelper;

    private Toolbar toolbar;

    private SmartRefreshLayout refreshLayout;

    private RecyclerView recyclerView;

    private List<HomeArticle> articleList = new ArrayList<>();

    private HomeAdapter homeAdapter;

    private int curPage = 0;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_article);
        initView();
        showProgressDialog();
        getTreeArticleJSON(0);
    }

    private void initView() {
        dataBaseHelper = MyDataBaseHelper.getInstance();
        toolbar = (Toolbar) findViewById(R.id.tree_article_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        refreshLayout = (SmartRefreshLayout) findViewById(R.id.tree_article_refresh_layout);
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new PhoenixHeader(MyApplication.getContext()));
        refreshLayout.setRefreshFooter(new BallPulseFooter(MyApplication.getContext()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                articleList.clear();
                getTreeArticleJSON(0);
                curPage = 0;
                refreshlayout.finishRefresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                curPage++;
                getTreeArticleJSON(curPage);
                refreshlayout.finishLoadMore();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.tree_article_recycler_view);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        homeAdapter = new HomeAdapter(articleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
    }

    private void getTreeArticleJSON(int page) {
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String id = intent.getStringExtra("id");
        toolbar.setTitle(name);
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/article" + "/list" + "/" + page + "/json?" + "cid=" + id,
                new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                            }
                        }).start();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        parseTreeArticleJSON(responseData);
                        if(!ActivityUtil.isDestroy(TreeArticleActivity.this)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    homeAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
    }

    private void parseTreeArticleJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONObject data = jsonData.getJSONObject("data");
            JSONArray datas = data.getJSONArray("datas");
            for (int i = 0; i < datas.length(); i++) {
                JSONObject jsonObject = datas.getJSONObject(i);
                HomeArticle homeArticle = new HomeArticle();
                homeArticle.setAuthor(jsonObject.getString("author"));
                homeArticle.setTitle(String.valueOf(Html.fromHtml(jsonObject.getString("title"))));
                homeArticle.setLink(jsonObject.getString("link"));
                homeArticle.setNiceDate(jsonObject.getString("niceDate"));
                homeArticle.setChapterName(jsonObject.getString("superChapterName"));
                JSONArray tags = jsonObject.getJSONArray("tags");
                if (tags.length() != 0) homeArticle.setTag(tags.getJSONObject(0).getString("name"));
                articleList.add(homeArticle);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    protected void onResume() {
        super.onResume();
        homeAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //saveReadHistory(articleList.get(position));
                String link = articleList.get(position).getLink();
                String title = articleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }

            @Override
            public void onItemCollectClick(int position) {}
        });
    }

    /**
     * 存储阅读历史
     */
    private void saveReadHistory(HomeArticle homeArticle) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("type", TYPE_ARTICLE);
        contentValues.put("author", homeArticle.getAuthor());
        contentValues.put("title", homeArticle.getTitle());
        contentValues.put("link", homeArticle.getLink());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        contentValues.put("read_date", dateFormat.format(date));
        contentValues.put("chapter_name", homeArticle.getChapterName());
        db.insert("History", null, contentValues);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
