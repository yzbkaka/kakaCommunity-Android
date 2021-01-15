package com.example.kakacommunity.community;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.model.CommunityReply;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;

import java.util.ArrayList;
import java.util.List;

public class ReplyDetailAdapter extends RecyclerView.Adapter<ReplyDetailAdapter.ViewHolder> {

    private List<CommunityReply> communityReplyList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout replyTargetLayout;
        ImageView imageView;
        TextView replyName;
        TextView replyTime;
        TextView replyContent;
        TextView replyTargetUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            replyTargetLayout = (LinearLayout) itemView.findViewById(R.id.comment_reply_target_layout);
            imageView = (ImageView) itemView.findViewById(R.id.comment_reply_detail_author_image);
            replyName = (TextView) itemView.findViewById(R.id.comment_reply_detail_author);
            replyTime = (TextView) itemView.findViewById(R.id.comment_reply_detail_time);
            replyContent = (TextView) itemView.findViewById(R.id.comment_reply_detail_content);
            replyTargetUser = (TextView) itemView.findViewById(R.id.comment_reply_target_user);
        }
    }

    public ReplyDetailAdapter(List<CommunityReply> communityReplyList) {
        this.communityReplyList = communityReplyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_detail_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityReply commentReply = communityReplyList.get(position);
        holder.replyName.setText(commentReply.getName());
        holder.replyTime.setText(commentReply.getTime());
        holder.replyContent.setText(commentReply.getContent());
        Glide.with(MyApplication.getContext())
                .load(commentReply.getImageUrl())
                .into(holder.imageView);

        if(commentReply.getTargetUser() != null) {
        }
        if (commentReply.getTargetUser() != null) {
            holder.replyTargetLayout.setVisibility(View.VISIBLE);
            holder.replyTargetUser.setText(commentReply.getTargetUser());
        }
    }

    @Override
    public int getItemCount() {
        return communityReplyList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}
