package com.example.kakacommunity.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.model.Banner;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class ImageAdapter extends BannerAdapter<Banner, ImageAdapter.ViewHolder> {

    private List<Banner> bannerList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.banner_image);
            textView = (TextView)view.findViewById(R.id.banner_title);

        }
    }

    public ImageAdapter(List<Banner> bannerList) {
        super(bannerList);
        this.bannerList = bannerList;
    }


    @Override
    public ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindView(ViewHolder holder, Banner data, int position, int size) {
        Banner banner = bannerList.get(position);
        Glide.with(MyApplication.getContext()).load(banner.getImagePath()).into(holder.imageView);
        holder.textView.setText(banner.getTitle());
    }
}
