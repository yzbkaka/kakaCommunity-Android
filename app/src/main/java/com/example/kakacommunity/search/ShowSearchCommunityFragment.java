package com.example.kakacommunity.search;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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
import com.example.kakacommunity.community.CommunityAdapter;
import com.example.kakacommunity.community.CommunityDetailActivity;
import com.example.kakacommunity.db.MyDataBaseHelper;
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

import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_COMMUNITY;

public class ShowSearchCommunityFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private SmartRefreshLayout refreshLayout;

    private String keyWord;

    private RecyclerView recyclerView;

    private CommunityAdapter communityAdapter;

    private List<HomeArticle> communityArticleList = new ArrayList<>();

    private int curPage = 0;

    private ImageView errorImage;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_search_community, container, false);
        initView(view);
        showProgressDialog();
        getSearchCommunityJSON();
        return view;
    }

    private void initView(View view) {
        dataBaseHelper = MyDataBaseHelper.getInstance();
        errorImage = (ImageView)view.findViewById(R.id.show_search_community_error);
        refreshLayout = (SmartRefreshLayout)view.findViewById(R.id.show_search_community_refresh_layout);
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new PhoenixHeader(MyApplication.getContext()));
        refreshLayout.setRefreshFooter(new BallPulseFooter(MyApplication.getContext()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                communityArticleList.clear();
                getSearchCommunityJSON();
                curPage = 0;
                refreshlayout.finishRefresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                curPage++;
                getSearchCommunityJSON();
                refreshlayout.finishLoadMore();

            }
        });
        recyclerView = (RecyclerView)view.findViewById(R.id.show_search_community_recycler_view);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        communityAdapter = new CommunityAdapter(communityArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(communityAdapter);
    }

    private void getSearchCommunityJSON() {
        Intent intent = getActivity().getIntent();
        keyWord = intent.getStringExtra("keyword");
        HttpUtil.OkHttpGET(BASE_ADDRESS + "/search" + "/" + keyWord, new okhttp3.Callback() {
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
                parseSearchCommunityJSON(responseData);
                if (!ActivityUtil.isDestroy(getActivity())) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            errorImage.setVisibility(View.GONE);
                            communityAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void parseSearchCommunityJSON(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            for(int i = 0;i < jsonArray.length();i++) {
                HomeArticle homeArticle = new HomeArticle();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject post = jsonObject.getJSONObject("post");
                homeArticle.setDiscussPostId(post.getString("id"));
                homeArticle.setTitle(post.getString("title"));
                homeArticle.setContent(post.getString("content"));
                homeArticle.setNiceDate(post.getString("createTime"));
                JSONObject user = jsonObject.getJSONObject("user");
                homeArticle.setAuthor(user.getString("username"));
                communityArticleList.add(homeArticle);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        communityAdapter.setOnItemCLickListener(new CommunityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                saveReadHistory(communityArticleList.get(position));
                String discussPostId = communityArticleList.get(position).getDiscussPostId();
                Intent intent = new Intent(MyApplication.getContext(), CommunityDetailActivity.class);
                intent.putExtra("discussPostId", discussPostId);
                startActivity(intent);
            }
        });
        errorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSearchCommunityJSON();
            }
        });
    }

    private void saveReadHistory(HomeArticle homeArticle) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("type", TYPE_COMMUNITY);
        contentValues.put("author", homeArticle.getAuthor());
        contentValues.put("title", homeArticle.getTitle());
        contentValues.put("link", homeArticle.getDiscussPostId());
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
