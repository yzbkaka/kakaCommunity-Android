package com.example.kakacommunity.mine;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.home.HomeAdapter;
import com.example.kakacommunity.model.HomeArticle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MineFragment extends Fragment {

    private RecyclerView recyclerView;

    private HomeAdapter homeAdapter;

    private ImageView backImage;

    private ImageView headImage;

    private TextView userName;

    private List<HomeArticle> articleList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.mine_recycler_view);
        backImage = (ImageView)view.findViewById(R.id.back_image);
        headImage = (ImageView)view.findViewById(R.id.head_image);
        userName = (TextView)view.findViewById(R.id.user_name);
        initRecyclerView();
        return view;
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        homeAdapter = new HomeAdapter(articleList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMineArticleJSON();
        homeAdapter.setOnItemCLickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });
    }

    private void getMineArticleJSON() {
        for(int i = 0;i < 20;i++) {
            HomeArticle homeArticle = new HomeArticle();
            homeArticle.setAuthor("yzbkaka");
            homeArticle.setTitle("双非的秋招总结-已拿offer");
            homeArticle.setNiceDate(new Date().toString());
            homeArticle.setTag("讨论区");
            articleList.add(homeArticle);
        }
    }

    private void parseMineArticeJSON() {

    }
}
