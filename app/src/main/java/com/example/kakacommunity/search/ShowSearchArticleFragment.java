package com.example.kakacommunity.search;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.PhoenixHeader;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.db.MyDataBaseHelper;
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
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;

public class ShowSearchArticleFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private SmartRefreshLayout refreshLayout;

    private String keyWord;

    private RecyclerView recyclerView;

    private List<HomeArticle> homeArticleList = new ArrayList<>();

    private HomeAdapter homeAdapter;

    private int curPage = 0;

    private ImageView errorImage;

    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_search_article, container, false);
        initView(view);
        showProgressDialog();
        getSearchArticleJSON(0);
        return view;
    }

    private void initView(View view) {
        errorImage = (ImageView)view.findViewById(R.id.show_search_article_error);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        refreshLayout = (SmartRefreshLayout)view.findViewById(R.id.show_search_article_refresh_layout);
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new PhoenixHeader(MyApplication.getContext()));
        refreshLayout.setRefreshFooter(new BallPulseFooter(MyApplication.getContext()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                homeArticleList.clear();
                getSearchArticleJSON(0);
                curPage = 0;
                refreshlayout.finishRefresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                curPage++;
                getSearchArticleJSON(curPage);
                refreshlayout.finishLoadMore();

            }
        });
        recyclerView = (RecyclerView)view.findViewById(R.id.show_search_article_recycler_view);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        homeAdapter = new HomeAdapter(homeArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
    }

    private void getSearchArticleJSON(int page) {
        Intent intent = getActivity().getIntent();
        keyWord = intent.getStringExtra("keyword");
        RequestBody requestBody = new FormBody.Builder()
                .add("k", keyWord)
                .build();
        HttpUtil.OkHttpPOST(ANDROID_ADDRESS + "/article" + "/query" + "/" + page + "/json",
                requestBody, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        if (!ActivityUtil.isDestroy(getActivity())) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    errorImage.setVisibility(View.VISIBLE);
                                    Toast.makeText(MyApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        parseSearchArticleJSON(responseData);
                        if (!ActivityUtil.isDestroy(getActivity())) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    errorImage.setVisibility(View.GONE);
                                    homeAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
    }

    private void parseSearchArticleJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONObject data = jsonData.getJSONObject("data");
            JSONArray datas = data.getJSONArray("datas");
            for (int i = 0; i < datas.length(); i++) {
                JSONObject jsonObject = datas.getJSONObject(i);
                HomeArticle homeArticle = new HomeArticle();
                homeArticle.setFresh(jsonObject.getBoolean("fresh"));
                String author = jsonObject.getString("author");
                if (author.length() == 0) {
                    author = jsonObject.getString("shareUser");
                }
                homeArticle.setAuthor(author);
                homeArticle.setTitle(String.valueOf(Html.fromHtml(jsonObject.getString("title"))));
                homeArticle.setLink(jsonObject.getString("link"));
                homeArticle.setNiceDate(jsonObject.getString("niceDate"));
                homeArticle.setChapterName(jsonObject.getString("chapterName"));
                JSONArray tags = jsonObject.getJSONArray("tags");
                if (tags.length() != 0) {
                    homeArticle.setTag(tags.getJSONObject(0).getString("name"));
                }
                homeArticleList.add(homeArticle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        homeAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //saveReadHistory(homeArticleList.get(position));
                String link = homeArticleList.get(position).getLink();
                String title = homeArticleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }

            @Override
            public void onItemCollectClick(int position) {

            }
        });
        errorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSearchArticleJSON(0);
            }
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
            progressDialog = new ProgressDialog(getActivity());
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
