package com.example.kakacommunity.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.model.HomeArticle;
import com.example.kakacommunity.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;

    private HomeAdapter homeAdapter;

    private List<HomeArticle> homeArticleList = new ArrayList<>();

    private int curPage = 0;


    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.home_recycler_view);
        getJSON();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 获得json数据
     */
    private void getJSON() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/article" + "/list" + "/" + curPage  + "/json", new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        parseJSON(responseData);
                    }
                });
                curPage++;
            }
        }).start();
    }

    /**
     * 解析json字符串
     */
    private void parseJSON(String responseData) {
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
                homeArticleList.add(homeArticle);
            }
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("initView", "initView");
                homeAdapter = new HomeAdapter(homeArticleList);
                LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(homeAdapter);
                homeAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String link = homeArticleList.get(position).getLink();
                        String title = homeArticleList.get(position).getTitle();
                        Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("link", link);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
