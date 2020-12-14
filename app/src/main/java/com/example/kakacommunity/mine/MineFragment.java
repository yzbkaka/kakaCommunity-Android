package com.example.kakacommunity.mine;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;

public class MineFragment extends Fragment implements View.OnClickListener {

    private LinearLayout myArticel;

    private LinearLayout web;

    private LinearLayout tree;

    private LinearLayout collect;

    private LinearLayout setting;

    private LinearLayout about;

    private LinearLayout exit;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        myArticel = (LinearLayout)view.findViewById(R.id.my_article);
        myArticel.setOnClickListener(this);
        web = (LinearLayout)view.findViewById(R.id.web);
        web.setOnClickListener(this);
        tree = (LinearLayout)view.findViewById(R.id.tree);
        tree.setOnClickListener(this);
        collect = (LinearLayout)view.findViewById(R.id.collect);
        collect.setOnClickListener(this);
        setting = (LinearLayout)view.findViewById(R.id.setting);
        setting.setOnClickListener(this);
        about = (LinearLayout)view.findViewById(R.id.about);
        about.setOnClickListener(this);
        exit = (LinearLayout)view.findViewById(R.id.exit);
        exit.setOnClickListener(this);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_article:
                Intent intent = new Intent(MyApplication.getContext(),MyArticleActivity.class);
                startActivity(intent);
                break;
            case R.id.web:
            case R.id.tree:
            case R.id.collect:
            case R.id.setting:
            case R.id.about:
            case R.id.exit:
        }
    }
}
