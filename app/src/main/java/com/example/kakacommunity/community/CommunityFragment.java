package com.example.kakacommunity.community;

import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.base.BaseFragment;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.header.PhoenixHeader;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.HomeArticle;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommunityFragment extends BaseFragment {

    private RefreshLayout refreshLayout;

    private RecyclerView recyclerView;

    private CommunityAdapter communityAdapter;

    private List<HomeArticle> communityArticleList = new ArrayList<>();

    private ImageView errorImage;

    private ProgressDialog progressDialog;

    private int curPage = 0;


    @Override
    protected int setContentView() {
        return R.layout.fragment_community;
    }

    @Override
    protected void lazyLoad() {
        View view = getContentView();
        errorImage = (ImageView)view.findViewById(R.id.community_error);
        refreshLayout = (RefreshLayout)view.findViewById(R.id.community_swipe_refresh_layout);
        initRefreshView();
        recyclerView = (RecyclerView)view.findViewById(R.id.community_recycler_view);
        initRecyclerView();

        showProgressDialog();
        getCommunityArticleJSON(0);
        communityAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = communityArticleList.get(position).getLink();
                String title = communityArticleList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), CommunityDetailActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }
        });
        errorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCommunityArticleJSON(0);
            }
        });
    }

    private void initRefreshView() {
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new PhoenixHeader(MyApplication.getContext()));
        refreshLayout.setRefreshFooter(new BallPulseFooter(MyApplication.getContext()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                communityArticleList.clear();
                getCommunityArticleJSON(0);
                refreshlayout.finishRefresh();

            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //getCommunityArticleJSON(curPage);
                refreshlayout.finishLoadMore();

            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        communityAdapter = new CommunityAdapter(communityArticleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(communityAdapter);
    }

    private void getCommunityArticleJSON(int page) {
        for(int i = 0;i < 20;i++) {
            HomeArticle homeArticle = new HomeArticle();
            homeArticle.setAuthor("yzbkaka");
            homeArticle.setTitle("双非的秋招总结-已拿offer");
            homeArticle.setContent("在未对抖音内存进行专项治理之前我们梳理了一下整体内存指标的绝对值和相对崩溃，发现占比都很高。另外，内存相关指标在去年春节活动时又再次激增达到历史新高，所以整体来看内存问题相当严峻，必须要对其进行专项治理。");
            homeArticle.setNiceDate(new Date().toString());
            homeArticle.setTag("讨论区");
            communityArticleList.add(homeArticle);
        }
        closeProgressDialog();
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
