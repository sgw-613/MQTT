package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ftp.DownloadCallback;
import com.example.myapplication.ftp.FtpUtil;
import com.example.myapplication.view.DownloadProgressDialog;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.LineViewHolder>
{
	private final Context context;
	private Activity activity;
	private final int resId;
	private final String[] viewFields;
	private final Constructor constructor;
	private final int[] columnIndexs;
	private List<Map<Integer, String>> cursorValues = new ArrayList<>();

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) //onReceive函数不能做耗时的事情，参考值：10s以内
		{
			int status = intent.getExtras().getInt("status");

			Toast ts;
			switch (status){

				case FtpUtil.SUCCESS_DOWNLOAD:
					ts = Toast.makeText(context,"文件下载成功",Toast.LENGTH_LONG);
					ts.show();
					break;
				case  FtpUtil.DOWNLOAD_FILE_ERROR:
					ts = Toast.makeText(context,"文件下载出错",Toast.LENGTH_LONG);
					ts.show();
					break;
				case  FtpUtil.FILE_NOT_EXIST:
					ts = Toast.makeText(context,"服务器文件不存在",Toast.LENGTH_LONG);
					ts.show();
					break;
			}
		}
	};


	public CursorRecyclerViewAdapter(Context context,Activity activity, Cursor cursor,
                                     @LayoutRes int resId, int[] columnIndexs, String[] viewFields, Constructor constructor)
	{
		Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: init");
		this.context = context;
		this.activity = activity;
		this.resId = resId;
		this.columnIndexs = columnIndexs;
		this.viewFields = viewFields;
		this.constructor = constructor;

		context.registerReceiver(mBroadcastReceiver, new IntentFilter("com.sim.downloadfile"));

		// 遍历Cursor中的每条记录,将这些记录封装到List集合中
		//Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: ");
		while (cursor.moveToNext())
		{
			Map<Integer, String> row = new HashMap<>();
			Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter:moveToNext ");
			for(int columnIndex : columnIndexs)
			{
				//Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: columnIndexs");
				// 从Cursor中取出查询结果
				String column = cursor.getString(columnIndex);
				Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: columnIndex = "+columnIndex);
				Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: column = "+column);
				row.put(columnIndex, column);
			}
			cursorValues.add(row);
		}

	}
	@NonNull @Override
	public LineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
	{
		//Log.d("sgw_d", "CursorRecyclerViewAdapter onCreateViewHolder: ");
		// 加载列表项对应的布局文件
		View item = LayoutInflater.from(context).inflate(resId,
				new LinearLayout(context), false);
		try {
			// 将item封装成ViewHolder的子类后返回
			//return constructor.newInstance(context, item);
			return new LineViewHolder(item);
		} catch (Exception e) {
			Log.d("sgw_d", "CursorRecyclerViewAdapter onCreateViewHolder: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public class LineViewHolder extends RecyclerView.ViewHolder
	{
		//TextView titleView;
		TextView sub_content;
		ImageView recycler_image;
		LinearLayout recycler_item;
		public LineViewHolder(View itemView)
		{
			super(itemView);
			//titleView = itemView.findViewById(R.id.sub_id);
			sub_content = itemView.findViewById(R.id.sub_content);
			recycler_image = itemView.findViewById(R.id.recycler_image);
			recycler_item = itemView.findViewById(R.id.recycler_item);
		}
	}


	@Override
	public void onBindViewHolder(LineViewHolder viewHolder, int position)
	{
		//Log.d("sgw_d", "CursorRecyclerViewAdapter onBindViewHolder: ");
		Class<?> clazz = viewHolder.getClass();
		String content_s = cursorValues.get(position).get(columnIndexs[0]).toString();
		TextView textView = viewHolder.sub_content;
		textView.setText(content_s);
		ImageView imageView = viewHolder.recycler_image;

				//隔行变色
		if (position % 2 == 0){
			textView.setBackgroundColor(Color.parseColor("#DADADA"));
		}else{
			textView.setBackgroundColor(Color.parseColor("#F5F5F5"));
		}

		if (content_s.endsWith(".mp4") || content_s.endsWith(".avi")){
			Log.d("sgw_d", "CursorRecyclerViewAdapter onBindViewHolder: .mp4 || .avi");
			String localFilePath = Utils.getSDPath() + File.separator + "sim" + File.separator + content_s;
			File localFile = new File(localFilePath);
			if (localFile.exists()){
				//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.setting);
				imageView.setBackground(context.getResources().getDrawable(R.drawable.play));
				imageView.setVisibility(View.VISIBLE);
			}else {
				//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.setting);
				imageView.setBackground(context.getResources().getDrawable(R.drawable.download));
				imageView.setVisibility(View.VISIBLE);
			}
		}else {
			//imageView.setBackground(context.getResources().getDrawable(R.drawable.show_pwd_image));
			imageView.setVisibility(View.INVISIBLE);
		}


		viewHolder.recycler_item.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String fileName = cursorValues.get(position).get(columnIndexs[0]).toString();
				String localFilePath = Utils.getLocalFilePath(fileName);
				File localFile = new File(localFilePath);
				if (localFile.exists()){
					//开始播放
					Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: " + localFile.getAbsolutePath());
					Intent intent = new Intent("com.sim.activity.playmove");
					intent.putExtra("filename", fileName);
					v.getContext().startActivity(intent);
				}else {
					showNormalDialog(v,position,imageView,fileName);
				}

			}
		});

	}

	private void startDownload(View v, int position,ImageView imageView,String fileName){
		Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: 本地文件不存在,开始下载");

		DownloadProgressDialog progressDialog = new DownloadProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// 设置ProgressDialog 标题
		progressDialog.setTitle("下载提示");
		// 设置ProgressDialog 提示信息
		progressDialog.setMessage("当前下载进度:");
		// 设置ProgressDialog 标题图标
		//progressDialog.setIcon(R.drawable.a);
		// 设置ProgressDialog 进度条进度
		// 设置ProgressDialog 的进度条是否不明确
		progressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		progressDialog.setCancelable(true);
		progressDialog.show();

		FtpUtil.startDownloadFtpFile(context, fileName, progressDialog,new DownloadCallback() {
			@Override
			public void updateDownloadImage(int status) {
				Intent intent = new Intent("com.sim.downloadfile");
				intent.putExtra("status", status);
				context.sendBroadcast(intent);
				if (status == FtpUtil.SUCCESS_DOWNLOAD){
					imageView.setBackground(context.getResources().getDrawable(R.drawable.play));
					imageView.setVisibility(View.VISIBLE);

					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progressDialog.hide();
						}
					});

				}
			}
		});
	}

	private void showNormalDialog(View v, int position,ImageView imageView,String fileName){
		/* @setIcon 设置对话框图标
		 * @setTitle 设置对话框标题
		 * @setMessage 设置对话框消息提示
		 * setXXX方法返回Dialog对象，因此可以链式设置属性
		 */
		final AlertDialog.Builder normalDialog =
				new AlertDialog.Builder(context);
		//normalDialog.setIcon(R.drawable.icon_dialog);
		normalDialog.setTitle("下载"+fileName);
		normalDialog.setMessage("点击确定开始下载");
		normalDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//...To-do
						startDownload(v,position,imageView,fileName);
					}
				});
		normalDialog.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//...To-do
					}
				});
		// 显示
		normalDialog.show();
	}


	@Override
	public int getItemCount()
	{
		return cursorValues.size();
	}




}