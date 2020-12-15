package com.example.kakacommunity.project;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.kakacommunity.model.ProjectTree;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentAdapter  extends FragmentStatePagerAdapter {

    private List<Fragment> fragmentList = new ArrayList<>();

    private List<ProjectTree> projectTreeList = new ArrayList<>();


    public TabFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, List<ProjectTree> projectTreeList) {
        super(fm);
        this.fragmentList = fragmentList;
        this.projectTreeList = projectTreeList;
    }

    /**
     * 获得碎片
     */
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    /**
     * 获得标题
     */
    @Override
    public CharSequence getPageTitle(int position){
        return projectTreeList.get(position).getName();
    }
}
