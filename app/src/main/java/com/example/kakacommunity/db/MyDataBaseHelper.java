package com.example.kakacommunity.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.kakacommunity.MyApplication;

public class MyDataBaseHelper extends SQLiteOpenHelper {

    private static MyDataBaseHelper dataBaseHelper;

    public static final String CREEATE_PROJECT_TREE = "create table ProjectTree ("
            + "id text primary key,"
            + "name text)";

    private Context context;

    public MyDataBaseHelper(@Nullable Context context, @Nullable String name,
                            @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public static MyDataBaseHelper getInstance(){
        if(dataBaseHelper == null){
            dataBaseHelper = new MyDataBaseHelper(MyApplication.getContext(),"kakaCommunityStore.db",null,1);
        }
        return dataBaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREEATE_PROJECT_TREE);
        Toast.makeText(context, "create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
