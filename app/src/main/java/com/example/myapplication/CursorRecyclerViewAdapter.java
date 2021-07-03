package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ftp.FtpUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.LineViewHolder>
{
	private final Context context;
	private final int resId;
	private final String[] viewFields;
	private final Constructor constructor;
	private final int[] columnIndexs;
	private List<Map<Integer, String>> cursorValues = new ArrayList<>();

	public CursorRecyclerViewAdapter(Context context, Cursor cursor,
                                     @LayoutRes int resId, int[] columnIndexs, String[] viewFields, Constructor constructor)
	{
		Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: init");
		this.context = context;
		this.resId = resId;
		this.columnIndexs = columnIndexs;
		this.viewFields = viewFields;
		this.constructor = constructor;
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

		if (content_s.endsWith(".mp4")){
			Log.d("sgw_d", "CursorRecyclerViewAdapter onBindViewHolder: .mp4");
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
					Intent intent = new Intent("com.sim.activity.playmove");
					intent.putExtra("filename", fileName);
					v.getContext().startActivity(intent);

				}else {
					Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: 本地文件不存在开始下载");
					FtpUtil.startDownloadFtpFile(fileName);
					imageView.setBackground(context.getResources().getDrawable(R.drawable.play));
					imageView.setVisibility(View.VISIBLE);
				}
			}
		});

	}
	@Override
	public int getItemCount()
	{
		return cursorValues.size();
	}



}