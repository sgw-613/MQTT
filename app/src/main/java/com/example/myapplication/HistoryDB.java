package com.example.myapplication;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HistoryDB extends SQLiteOpenHelper {

    //数据库版本号
    private static Integer Version = 1;
    private static String DB_Name = "history.db";
    public static String SUB_CONTENT = "sub_content";
    public static String TABLE = "history";


    //在SQLiteOpenHelper的子类当中，必须有该构造函数
    //参数说明
    //context:上下文对象
    //name:数据库名称
    //param:factory
    //version:当前数据库的版本，值必须是整数并且是递增的状态
    public HistoryDB(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
        //必须通过super调用父类当中的构造函数
        super(context, name, factory, version);
    }

    public HistoryDB(Context context,String name,int version)
    {
        this(context,name,null,version);
    }


    public HistoryDB(Context context,String name)
    {
        this(context, DB_Name, Version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("sgw_d", "HistoryDB onCreate: 创建数据库和表");
        //创建了数据库并创建一个叫records的表
        //SQLite数据创建支持的数据类型： 整型数据，字符串类型，日期类型，二进制的数据类型
        String sql = "create table history(id int primary key,sub_content varchar(200))";
        //execSQL用于执行SQL语句
        //完成数据库的创建
        db.execSQL(sql);
        //数据库实际上是没有被创建或者打开的，直到getWritableDatabase() 或者 getReadableDatabase() 方法中的一个被调用时才会进行创建或者打开

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
