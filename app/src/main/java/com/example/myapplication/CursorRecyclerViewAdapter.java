package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.myapplication.vitam.MediaItem;

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
	private ArrayList<MediaItem> mediaItems;


	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) //onReceive?????????????????????????????????????????????10s??????
		{
			int status = intent.getExtras().getInt("status");

			Toast ts;
			switch (status){

				case FtpUtil.SUCCESS_DOWNLOAD:
					ts = Toast.makeText(context,"??????????????????",Toast.LENGTH_LONG);
					ts.show();
					break;
				case  FtpUtil.DOWNLOAD_FILE_ERROR:
					ts = Toast.makeText(context,"??????????????????",Toast.LENGTH_LONG);
					ts.show();
					break;
				case  FtpUtil.FILE_NOT_EXIST:
					ts = Toast.makeText(context,"????????????????????????",Toast.LENGTH_LONG);
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

		// ??????Cursor??????????????????,????????????????????????List?????????
		//Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: ");
		while (cursor.moveToNext())
		{
			Map<Integer, String> row = new HashMap<>();
			Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter:moveToNext ");
			for(int columnIndex : columnIndexs)
			{
				//Log.d("sgw_d", "CursorRecyclerViewAdapter CursorRecyclerViewAdapter: columnIndexs");
				// ???Cursor?????????????????????
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
		// ????????????????????????????????????
		View item = LayoutInflater.from(context).inflate(resId,
				new LinearLayout(context), false);
		try {
			// ???item?????????ViewHolder??????????????????
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

				//????????????
		if (position % 2 == 0){
			textView.setBackgroundColor(Color.parseColor("#DADADA"));
		}else{
			textView.setBackgroundColor(Color.parseColor("#F5F5F5"));
		}

		if (content_s.contains(".avi") || content_s.contains(".avi")){
			Log.d("sgw_d", "CursorRecyclerViewAdapter onBindViewHolder: .avi || .avi");
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

		String file_Name = cursorValues.get(position).get(columnIndexs[0]).toString();
		Log.d("sgw_d", "CursorRecyclerViewAdapter onBindViewHolder: file_Name="+file_Name);
		String fileName;
		if (file_Name.contains(".avi")){
			fileName = file_Name.substring(0,file_Name.lastIndexOf(".avi"))+".avi";

		}else{
			fileName = file_Name;
		}


		Log.d("sgw_d", "CursorRecyclerViewAdapter onBindViewHolder: fileName="+fileName);
		if (fileName.contains(".avi") || fileName.endsWith(".avi")){
			viewHolder.recycler_item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: "+fileName);
					String localFilePath = Utils.getLocalFilePath(fileName);
					File localFile = new File(localFilePath);
					if (localFile.exists()){
						getDataFromLocal(fileName,localFilePath);
						MediaItem mediaItem = mediaItems.get(0);
						Intent intent = new Intent(context,VitamioVideoPlayer.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("videolist",mediaItems);
						intent.putExtras(bundle);
						intent.putExtra("position",position);
						context.startActivity(intent);


//						//????????????????????????
//						Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: " + localFile.getAbsolutePath());
//						Intent intent = new Intent("com.sim.activity.playmove");
//						intent.putExtra("filename", fileName);
//						v.getContext().startActivity(intent);
					}else {
						showNormalDialog(v,position,imageView,fileName);
					}

				}
			});
		}


	}

	private void startDownload(View v, int position,ImageView imageView,String fileName){
		Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: ?????????????????????,????????????");

		DownloadProgressDialog progressDialog = new DownloadProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// ??????ProgressDialog ??????
		progressDialog.setTitle("????????????");
		// ??????ProgressDialog ????????????
		progressDialog.setMessage("??????????????????:");
		// ??????ProgressDialog ????????????
		//progressDialog.setIcon(R.drawable.a);
		// ??????ProgressDialog ???????????????
		// ??????ProgressDialog ???????????????????????????
		progressDialog.setIndeterminate(false);
		// ??????ProgressDialog ?????????????????????????????????
		progressDialog.setCancelable(true);
		progressDialog.show();

		FtpUtil.startDownloadFtpFile(context, fileName, progressDialog,new DownloadCallback() {
			@Override
			public void updateDownloadImage(int status) {
				Intent intent = new Intent("com.sim.downloadfile");
				intent.putExtra("status", status);
				context.sendBroadcast(intent);

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						imageView.setBackground(context.getResources().getDrawable(R.drawable.play));
						imageView.setVisibility(View.VISIBLE);
						progressDialog.hide();
					}
				});
			}
		});
	}

	private void showNormalDialog(View v, int position,ImageView imageView,String fileName){
		/* @setIcon ?????????????????????
		 * @setTitle ?????????????????????
		 * @setMessage ???????????????????????????
		 * setXXX????????????Dialog???????????????????????????????????????
		 */
		final AlertDialog.Builder normalDialog =
				new AlertDialog.Builder(context);
		//normalDialog.setIcon(R.drawable.icon_dialog);
		normalDialog.setTitle("??????"+fileName);
		normalDialog.setMessage("????????????????????????");
		normalDialog.setPositiveButton("??????",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//...To-do

						String file_name = fileName.substring(0,fileName.lastIndexOf(".avi")) + ".avi";
						Log.d("sgw_d", "CursorRecyclerViewAdapter onClick: file_name="+file_name);

						startDownload(v,position,imageView,file_name);
					}
				});
		normalDialog.setNegativeButton("??????",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//...To-do
					}
				});
		// ??????
		normalDialog.show();
	}


	@Override
	public int getItemCount()
	{
		return cursorValues.size();
	}


	/**
	 * ????????????sdcard????????????
	 * //1.??????sdcard,?????????
	 * //2.????????????????????????????????????
	 * //3.?????????6.0??????????????????????????????sdcard?????????
	 */
	private void getDataFromLocal(String name,String data) {
		Log.d("sgw_d", "CursorRecyclerViewAdapter getDataFromLocal: data="+data);
		if (mediaItems == null){
			mediaItems = new ArrayList<>();
		}else {
			mediaItems.clear();
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] objs = {
				MediaStore.Video.Media.DISPLAY_NAME,//???????????????sdcard?????????
				MediaStore.Video.Media.DURATION,//???????????????
				MediaStore.Video.Media.SIZE,//?????????????????????
				MediaStore.Video.Media.DATA,//?????????????????????
				MediaStore.Video.Media.ARTIST,//??????????????????

		};
		MediaItem mediaItem = new MediaItem();
		mediaItems.add(mediaItem);//????????????
		//String name = cursor.getString(0);//???????????????
		mediaItem.setName(name);
		//long duration = cursor.getLong(1);//???????????????
//		mediaItem.setDuration(duration);
//		//long size = cursor.getLong(2);//?????????????????????
//		mediaItem.setSize(size);
//		//String data = cursor.getString(3);//?????????????????????
		mediaItem.setData(data);
//		//String artist = cursor.getString(4);//?????????
//		mediaItem.setArtist(artist);
	}

}