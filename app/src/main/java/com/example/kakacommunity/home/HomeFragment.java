package com.example.kakacommunity.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.model.Banner;
import com.example.kakacommunity.model.HomeArticle;
import com.example.kakacommunity.utils.HttpUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.wingsofts.byeburgernavigationview.ByeBurgerBehavior;
import com.youth.banner.indicator.CircleIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.MainActivity.bottomNavigationView;
import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.HOME_TOP;

public class HomeFragment extends Fragment {

    private HomeBroadcastReceiver homeBroadcastReceiver;

    private RefreshLayout refreshLayout;

    private com.youth.banner.Banner bannerView;

    private ImageAdapter bannerAdapter;

    private NestedScrollView nestedScrollView;

    private RecyclerView recyclerView;

    private HomeAdapter homeAdapter;

    private List<HomeArticle> homeArticleList = new ArrayList<>();

    private List<Banner> bannerList = new ArrayList<>();

    private int newPage = 0;

    private int curPage = 0;


    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        homeBroadcastReceiver = new HomeBroadcastReceiver();
        refreshLayout = (RefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        nestedScrollView = (NestedScrollView)view.findViewById(R.id.nest_scroll_view);
        initRefreshView();
        bannerView = (com.youth.banner.Banner)view.findViewById(R.id.banner_view);
        initBannerView();
        recyclerView = (RecyclerView)view.findViewById(R.id.home_recycler_view);
        initRecyclerView();
        return view;
    }

    private void initRefreshView() {
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        //refreshLayout.setRefreshHeader(new BezierRadarHeader(MyApplication.getContext()).setEnableHorizontalDrag(true));
        refreshLayout.setRefreshHeader(new ClassicsHeader(MyApplication.getContext()).setAccentColorId(R.color.white));
        refreshLayout.setRefreshFooter(new ClassicsFooter(MyApplication.getContext()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                homeArticleList.clear();
                refreshHomeArticleJSON();
                bannerList.clear();
                getBannerJSON();
                refreshlayout.finishRefresh();

            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                getHomeArticleJSON();
                refreshlayout.finishLoadMore();

            }
        });
    }

    private void initBannerView() {
        bannerAdapter = new ImageAdapter(bannerList);
        bannerView.addBannerLifecycleObserver(this) //添加生命周期观察者
                .setAdapter(bannerAdapter)
                .setIndicator(new CircleIndicator(MyApplication.getContext()));
        bannerAdapter.setOnItemCLickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String url = bannerList.get(position).getUrl();
                String title = bannerList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        homeAdapter = new HomeAdapter(homeArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getHomeArticleJSON();
        getBannerJSON();
        homeAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = homeArticleList.get(position).getLink();
                String title = homeArticleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }
        });
        /*nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY - oldScrollY > 0) {
                    bottomNavigationView.startAnimation(hideAnimation);
                    bottomNavigationView.setVisibility(View.INVISIBLE);

                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    bottomNavigationView.startAnimation(showAnimation);
                }
            }
        });*/

    }

    /**
     * 获得首页文章json数据
     */
    private void getHomeArticleJSON() {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/article" + "/list" + "/" + curPage  + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseHomeArticleJSON(responseData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        curPage++;
    }

    /**
     * 获得banner的json数据
     */
    private void getBannerJSON() {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/banner" + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseBannerJSON(responseData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * 解析文章json字符串
     */
    private void parseHomeArticleJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONObject data = jsonData.getJSONObject("data");
            JSONArray datas = data.getJSONArray("datas");
            for(int i = 0;i < datas.length();i++) {
                JSONObject jsonObject = datas.getJSONObject(i);
                HomeArticle homeArticle = new HomeArticle();
                homeArticle.setAuthor(jsonObject.getString("author"));
                homeArticle.setTitle(jsonObject.getString("title"));
                homeArticle.setLink(jsonObject.getString("link"));
                homeArticle.setNiceDate(jsonObject.getString("niceDate"));
                homeArticle.setChapterName(jsonObject.getString("chapterName"));
                JSONArray tags = jsonObject.getJSONArray("tags");
                if(tags.length() != 0) homeArticle.setTag(tags.getJSONObject(0).getString("name"));
                homeArticleList.add(homeArticle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析banner的json字符串
     */
    private void parseBannerJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONArray datas = jsonData.getJSONArray("data");
            for(int i = 0;i < datas.length();i++) {
                JSONObject jsonObject = datas.getJSONObject(i);
                Banner banner = new Banner();
                banner.setImagePath(jsonObject.getString("imagePath"));
                banner.setTitle(jsonObject.getString("title"));
                banner.setUrl(jsonObject.getString("url"));
                bannerList.add(banner);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshHomeArticleJSON() {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/article" + "/list" + "/" + newPage + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseHomeArticleJSON(responseData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    class HomeBroadcastReceiver extends BroadcastReceiver {
        public HomeBroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(HOME_TOP);
            getActivity().registerReceiver(this, intentFilter);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            nestedScrollView.smoothScrollTo(0, 0);
        }
    }
}