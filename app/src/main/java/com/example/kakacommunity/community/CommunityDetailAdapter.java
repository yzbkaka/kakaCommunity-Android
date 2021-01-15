package com.example.kakacommunity.community;

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
import com.example.kakacommunity.model.CommunityComment;

import java.util.ArrayList;
import java.util.List;

public class CommunityDetailAdapter extends RecyclerView.Adapter<CommunityDetailAdapter.ViewHolder> {

    private List<CommunityComment> communityCommentList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView imageView;
        TextView replyName;
        TextView replyTime;
        TextView replyContent;
        TextView replyCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (LinearLayout)itemView.findViewById(R.id.community_reply_layout);
            imageView = (ImageView) itemView.findViewById(R.id.community_reply_author_image);
            replyName = (TextView) itemView.findViewById(R.id.community_reply_author);
            replyTime = (TextView) itemView.findViewById(R.id.community_reply_time);
            replyContent = (TextView) itemView.findViewById(R.id.community_reply_content);
            replyCount = (TextView)itemView.findViewById(R.id.community_reply_comment_count);
        }
    }

    public CommunityDetailAdapter(List<CommunityComment> communityCommentList) {
        this.communityCommentList = communityCommentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.commuity_reply_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityComment communityComment = communityCommentList.get(position);
        holder.replyName.setText(communityComment.getName());
        holder.replyTime.setText(communityComment.getTime());
        holder.replyContent.setText(communityComment.getContent());
        Glide.with(MyApplication.getContext())
                .load(communityComment.getImageUrl())
                .into(holder.imageView);
        if(communityComment.getCommentReplyList().size() != 0) {
            holder.replyCount.setVisibility(View.VISIBLE);
            holder.replyCount.setText("查看" + communityComment.getReplyCount() + "条回复");
        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return communityCommentList.size();
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}
