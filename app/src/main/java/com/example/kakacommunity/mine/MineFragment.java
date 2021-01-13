package com.example.kakacommunity.mine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.kakacommunity.LoginActivity;
import com.example.kakacommunity.base.BaseFragment;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.mine.about.AboutActivity;
import com.example.kakacommunity.mine.collect.CollectActivity;
import com.example.kakacommunity.mine.history.HistoryActivity;
import com.example.kakacommunity.mine.marticle.MyArticleActivity;
import com.example.kakacommunity.mine.tree.TreeActivity;
import com.example.kakacommunity.mine.web.UseWebActivity;
import com.example.kakacommunity.utils.HttpUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.PrimitiveIterator;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.LoginActivity.ticket;
import static com.example.kakacommunity.constant.kakaCommunityConstant.BASE_ADDRESS;


public class MineFragment extends BaseFragment implements View.OnClickListener {

    private CircleImageView userImage;

    private TextView userName;

    private String headerUrl;

    private String username;

    public static String userId;

    private LinearLayout myArticle;

    private LinearLayout web;

    private LinearLayout tree;

    private LinearLayout collect;

    private LinearLayout setting;

    private LinearLayout about;

    private LinearLayout exit;

/*    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        myArticle = (LinearLayout) view.findViewById(R.id.my_article);
        myArticle.setOnClickListener(this);
        web = (LinearLayout) view.findViewById(R.id.web);
        web.setOnClickListener(this);
        tree = (LinearLayout) view.findViewById(R.id.tree);
        tree.setOnClickListener(this);
        collect = (LinearLayout) view.findViewById(R.id.collect);
        collect.setOnClickListener(this);
        setting = (LinearLayout) view.findViewById(R.id.history);
        setting.setOnClickListener(this);
        about = (LinearLayout) view.findViewById(R.id.about);
        about.setOnClickListener(this);
        exit = (LinearLayout) view.findViewById(R.id.exit);
        exit.setOnClickListener(this);
        return view;
    }*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("mine", "create");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void lazyLoad() {
        View view = getContentView();
        userImage = (CircleImageView)view.findViewById(R.id.user_image);
        userName = (TextView)view.findViewById(R.id.user_name);
        myArticle = (LinearLayout) view.findViewById(R.id.my_article);
        myArticle.setOnClickListener(this);
        web = (LinearLayout) view.findViewById(R.id.web);
        web.setOnClickListener(this);
        tree = (LinearLayout) view.findViewById(R.id.tree);
        tree.setOnClickListener(this);
        collect = (LinearLayout) view.findViewById(R.id.collect);
        collect.setOnClickListener(this);
        setting = (LinearLayout) view.findViewById(R.id.history);
        setting.setOnClickListener(this);
        about = (LinearLayout) view.findViewById(R.id.about);
        about.setOnClickListener(this);
        exit = (LinearLayout) view.findViewById(R.id.exit);
        exit.setOnClickListener(this);
        getUserMessageJSON();
    }

    private void getUserMessageJSON() {
        HttpUtil.OkHttpGET(BASE_ADDRESS + "/user"  + "/" + ticket, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("mine",responseData);
                parseUserMessageJSON(responseData);
            }
        });
    }

    private void parseUserMessageJSON(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject jsonUser = jsonObject.getJSONObject("user");
            headerUrl = jsonUser.getString("headerUrl");
            username = jsonUser.getString("username");
            userId = jsonUser.getString("id");
        }catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.with(MyApplication.getContext())
                        .load(headerUrl)
                        .into(userImage);
                userName.setText(username);
            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_article:
                Intent intent1 = new Intent(MyApplication.getContext(), MyArticleActivity.class);
                startActivity(intent1);
                break;
            case R.id.web:
                Intent intent2 = new Intent(MyApplication.getContext(), UseWebActivity.class);
                startActivity(intent2);
                break;
            case R.id.tree:
                Intent intent3 = new Intent(MyApplication.getContext(), TreeActivity.class);
                startActivity(intent3);
                break;
            case R.id.collect:
                Intent intent4 = new Intent(MyApplication.getContext(), CollectActivity.class);
                startActivity(intent4);
                break;
            case R.id.history:
                Intent intent5 = new Intent(MyApplication.getContext(), HistoryActivity.class);
                startActivity(intent5);
                break;
            case R.id.about:
                Intent intent6 = new Intent(MyApplication.getContext(), AboutActivity.class);
                startActivity(intent6);
            case R.id.exit:
                Intent intent7 = new Intent(MyApplication.getContext(), LoginActivity.class);
                startActivity(intent7);
                getActivity().finish();
                break;
        }
    }

}
