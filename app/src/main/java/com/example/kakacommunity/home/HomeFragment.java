package com.example.kakacommunity.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.model.HomeArticle;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;

    private HomeAdapter homeAdapter;

    private List<HomeArticle> homeArticleList;


    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.home_recycler_view);
        homeAdapter = new HomeAdapter(homeArticleList);
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
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
