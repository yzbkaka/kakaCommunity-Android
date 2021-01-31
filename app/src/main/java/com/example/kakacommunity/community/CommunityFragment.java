package com.example.kakacommunity.community;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.base.BaseFragment;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.header.PhoenixHeader;
import com.example.kakacommunity.model.HomeArticle;
import com.example.kakacommunity.utils.ActivityUtil;
import com.example.kakacommunity.utils.HttpUtil;
import com.scwang.smart.refresh.footer.BallPulseFooter;
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

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CAMERA_SERVICE;
import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.COMMUNITY_ADD;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_COMMUNITY;

public class CommunityFragment extends BaseFragment {

    private MyDataBaseHelper dataBaseHelper;

    private CommunityBroadcastReceiver communityBroadcastReceiver;

    private RefreshLayout refreshLayout;

    private RecyclerView recyclerView;

    private CommunityAdapter communityAdapter;

    private List<HomeArticle> communityArticleList = new ArrayList<>();

    private ImageView errorImage;

    private ProgressDialog progressDialog;

    private int curPage = 1;

    public static final int COMMUNITY_FRAGMENT_CODE = 1;

    public static final int COMMUNITY_FRAGMENT_NOTIFY = 2;

    @Override
    protected int setContentView() {
        return R.layout.fragment_community;
    }

    @Override
    protected void lazyLoad() {
        View view = getContentView();
        dataBaseHelper = MyDataBaseHelper.getInstance();
        communityBroadcastReceiver = new CommunityBroadcastReceiver();
        errorImage = (ImageView) view.findViewById(R.id.community_error);
        refreshLayout = (RefreshLayout) view.findViewById(R.id.community_swipe_refresh_layout);
        initRefreshView();
        recyclerView = (RecyclerView) view.findViewById(R.id.community_recycler_view);
        initRecyclerView();

        showProgressDialog();
        getCommunityJSON(1);
        communityAdapter.setOnItemCLickListener(new CommunityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //saveReadHistory(communityArticleList.get(position));
                String discussPostId = communityArticleList.get(position).getDiscussPostId();
                Intent intent = new Intent(MyApplication.getContext(), CommunityDetailActivity.class);
                intent.putExtra("discussPostId", discussPostId);
                startActivityForResult(intent,COMMUNITY_FRAGMENT_NOTIFY);
            }

            @Override
            public void onItemCollectClick(int position) {

            }
        });
        errorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCommunityJSON(1);
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
                getCommunityJSON(1);
                curPage = 1;
                refreshlayout.finishRefresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                curPage++;
                getCommunityJSON(curPage);
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

    private void getCommunityJSON(int page) {
        HttpUtil.OkHttpGET(BASE_ADDRESS + "/index" + "/" + page, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        errorImage.setVisibility(View.VISIBLE);
                        Toast.makeText(MyApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseCommunityJSON(responseData);
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

    private void parseCommunityJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONArray discussPosts = jsonData.getJSONArray("discussPosts");
            for (int i = 0; i < discussPosts.length(); i++) {
                JSONObject jsonObject = discussPosts.getJSONObject(i);
                JSONObject discussPost = jsonObject.getJSONObject("discussPost");  //解析文章数据
                HomeArticle homeArticle = new HomeArticle();
                homeArticle.setDiscussPostId(discussPost.getString("id"));
                homeArticle.setTitle(discussPost.getString("title"));
                homeArticle.setContent(discussPost.getString("content"));
                homeArticle.setNiceDate(discussPost.getString("createTime"));
                JSONObject user = jsonObject.getJSONObject("user");
                homeArticle.setAuthor(user.getString("username"));
                communityArticleList.add(homeArticle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case COMMUNITY_FRAGMENT_CODE:
                if (resultCode == RESULT_OK) {
                    communityArticleList.clear();
                    getCommunityJSON(1);
                }
                break;
            case COMMUNITY_FRAGMENT_NOTIFY:
                communityArticleList.clear();
                getCommunityJSON(1);
                break;
            default:
        }
    }

    class CommunityBroadcastReceiver extends BroadcastReceiver {

        public CommunityBroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(COMMUNITY_ADD);
            getActivity().registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intent1 = new Intent(MyApplication.getContext(), AddCommunityActivity.class);
            startActivityForResult(intent1, COMMUNITY_FRAGMENT_CODE);
        }
    }
}
