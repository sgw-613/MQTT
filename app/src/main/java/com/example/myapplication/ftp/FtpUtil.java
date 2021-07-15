package com.example.myapplication.ftp;


import android.content.Context;
import android.util.Log;

import com.example.myapplication.EditActivity;
import com.example.myapplication.Utils;
import com.example.myapplication.view.DownloadProgressDialog;

import java.io.*;
import java.net.SocketException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

//import cn.com.jit.cloud.common.exception.BaseException;

public class FtpUtil {

    public static final int SUCCESS_DOWNLOAD = 0;
    public static final int CANCELLED_DOWNLOAD = 1;
    public static final int FILE_NOT_EXIST = 2;
    public static final int DOWNLOAD_FILE_ERROR = 3;
    public static final int CONNECT_FTP_ERROR = 4;
    private Context mContext;

    public DownLoadProgressListener downLoadProgressListener;

    private boolean cancelled = false;

    public FtpUtil(Context context) {
        this.mContext = context;
    }

    public FtpUtil() {

    }

    public FtpUtil setDownLoadProgressListener(DownLoadProgressListener downLoadProgressListener) {
        this.downLoadProgressListener = downLoadProgressListener;
        return this;
    }

    //private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    /**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                Log.d("sgw_d", "未连接到FTP，用户名或密码错误。");
                //System.out.println("未连接到FTP，用户名或密码错误。");
                return null;
            } else {
                Log.d("sgw_d", "FTP connection success");
            }
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d("sgw_d","FTP的IP地址可能错误，请正确配置。");
            return null;
        } catch (IOException e) {
            Log.d("sgw_d","FTP的IP地址可能错误，请正确配置。");
            return null;
        }
        return ftpClient;
    }

    /*
     * 从FTP服务器获取文件
     *
     * @param ftpHost FTP IP地址
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpPort FTP端口
     * @param ftpPath FTP服务器中文件所在路径 格式： /ftptest/aa.pdf
     */
    public byte[] getFtpFileBytes(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort, String ftpPath) {
        FTPClient ftpClient = null;
        try {
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            //ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 使用二进制保存方式
            /*
             * 这个方法的意思就是每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据。
             * 为什么要这样做呢，因为ftp server可能每次开启不同的端口来传输数据，
             * 但是在linux上，由于安全限制，可能某些端口没有开启，所以就出现阻塞。
             */
            ftpClient.enterLocalPassiveMode();
            InputStream inputStream = ftpClient.retrieveFileStream(ftpPath);
            if (null == inputStream) {
                System.out.println("没有找到" + ftpPath + "文件");
            }
            byte[] bytes = IOUtils.toByteArray(inputStream);
            inputStream.close();
            ftpClient.logout();
            return bytes;
        } catch (FileNotFoundException e) {
            System.out.println("没有找到" + ftpPath + "文件");
        } catch (SocketException e) {
            System.out.println("连接FTP失败.");
        } catch (IOException e) {
            System.out.println("文件读取错误。");
        }
        return null;
    }

    /*
     * 从FTP服务器下载文件
     *
     * @param ftpHost FTP IP地址
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpPort FTP端口
     * @param ftpPath FTP服务器中文件所在路径 格式： ftptest/aa
     * @param localPath 下载到本地的位置 格式：H:/download
     * @param fileName 文件名称
     */
    public int downloadFtpFile(String ftpHost, String ftpUserName, String ftpPassword,
                                int ftpPort, String ftpPath, String localPath, String fileName) {
        FTPClient ftpClient = null;
        Long fileSize = -1L;
        try {
            Log.d("sgw_d", "FtpUtil downloadFtpFile: getFTPClient");
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            if (ftpClient == null){
                //连接服务器错误
                return CONNECT_FTP_ERROR;
            }
            Log.d("sgw_d", "FtpUtil downloadFtpFile: getFTPClient end");

            //ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 使用二进制保存方式
            ftpClient.enterLocalPassiveMode();

            Log.d("sgw_d", "FtpUtil downloadFtpFile: ftpPath = "+ftpPath);
            Log.d("sgw_d", "FtpUtil downloadFtpFile: fileName = "+fileName);

            ftpClient.changeWorkingDirectory(ftpPath);

            FTPFile[] ftpFiles = ftpClient.listFiles(fileName);

            if(null != ftpFiles && ftpFiles.length > 0)
            {
                //file　存在
                fileSize = ftpFiles[0].getSize();
            }else {
                return FILE_NOT_EXIST; //ftp服务器不存在file返回1
            }
            for (FTPFile ftpFile: ftpFiles){
                Log.d("sgw_d", "FtpUtil downloadFtpFile: ftpFile.getName = "+ftpFile.getName());
            }

            Log.d("sgw_d", "FtpUtil downloadFtpFile: ftpPath = " + ftpPath);

            File localFileDir = new File(localPath);
            if (!localFileDir.exists()){
                localFileDir.mkdir();
            }

            File localFile = new File(localPath + File.separatorChar + fileName);
            OutputStream os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(fileName, os);
            os.close();
            ftpClient.logout();
            Log.d("sgw_d", "FtpUtil downloadFtpFile: end");
        } catch (FileNotFoundException e) {
            Log.d("sgw_d", "downloadFtpFile 没有找到:" + e);

//            throw new BaseException("FF1C1807", fileName);
        } catch (SocketException e) {
            Log.d("sgw_d", "连接FTP失败.");
//            throw new BaseException("FF1C1808");
        } catch (IOException e) {
            Log.d("sgw_d", "文件读取错误。");
//            throw new BaseException("FF1C1809");
        }
        return SUCCESS_DOWNLOAD; //成功返回0
    }



//    protected void setNotification(int progress,Context context) {
//        if (this==null){
//            return;
//        }
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_notification_view);
//        remoteViews.setTextViewText(R.id.tvTitle, "正在下载：");
//        remoteViews.setProgressBar(R.id.pb, 100, progress, false);
//        NotificationUtils notificationUtils = new NotificationUtils(context);
//        NotificationManager manager = notificationUtils.getManager();
//        Notification notification = notificationUtils
//                .setContent(remoteViews)
//                .setFlags(Notification.FLAG_AUTO_CANCEL)
//                .setOnlyAlertOnce(true)
//                .getNotification("来了一条消息", "下载apk", R.mipmap.ic_launcher);
//        //下载成功或者失败
//        if (progress == 100 || progress == -1) {
//            notificationUtils.clearNotification();
//        } else {
//            manager.notify(1, notification);
//        }
//    }


    /*
     * 从FTP服务器下载文件
     *
     * @param ftpHost FTP IP地址
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpPort FTP端口
     * @param ftpPath FTP服务器中文件所在路径 格式： ftptest/aa
     * @param localPath 下载到本地的位置 格式：H:/download
     * @param fileName 文件名称
     */
    public int downloadFtpFileProgress(String ftpHost, String ftpUserName, String ftpPassword,
                                int ftpPort, String ftpPath, String localPath, String fileName,DownloadProgressDialog progressDialog) {
        FTPClient ftpClient = null;

        long fileSize = 0;
        long step = 0;
        long process = 0;
        long currentSize = 0;


        try {
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            //ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 使用二进制保存方式
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);
            Log.d("sgw_d", "FtpUtil downloadFtpFileProgress: ftpPath =" +ftpPath);
            FTPFile[] ftpFiles = ftpClient.listFiles(fileName);
            if(null != ftpFiles && ftpFiles.length > 0)
            {
                //file　存在
                fileSize = ftpFiles[0].getSize();
                Log.d("sgw_d", "FtpUtil downloadFtpFileProgress:"+fileName+"服务器存在");
            }else {
                Log.d("sgw_d", "FtpUtil downloadFtpFileProgress: ftp服务器不存在file返回1");
                return FILE_NOT_EXIST; //ftp服务器不存在file返回1
            }

           step = fileSize / 100;

            Log.d("sgw_d", "FtpUtil downloadFtpFileProgress: ftpPath = " + ftpPath);

            File localFileDir = new File(localPath);
            if (!localFileDir.exists()){
                localFileDir.mkdir();
            }

            File localFile = new File(localPath + File.separatorChar + fileName);
            OutputStream os = new FileOutputStream(localFile);
            InputStream inputStream = ftpClient.retrieveFileStream(fileName);
            progressDialog.setMax((int) fileSize);

            byte[] data = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    inputStream.close();
                    return CANCELLED_DOWNLOAD;
                }
                currentSize += count;
                process = currentSize / step;
                if (process % 5 == 0) {
                        progressDialog.setProgress((int) currentSize);
                    //Log.d("sgw_d", "FtpUtil downloadFtpFileProgress: process = "+process);
                }

                os.write(data, 0, count);
            }


            //progressDialog.hide();
            os.close();
            ftpClient.logout();
            Log.d("sgw_d", "FtpUtil downloadFtpFileProgress: end");
        } catch (Exception e) {
            Log.d("sgw_d", "downloadFtpFileProgress 没有找到:" + e);
            return DOWNLOAD_FILE_ERROR;
        }
        Log.d("sgw_d", "FtpUtil downloadFtpFileProgress: end");
        return SUCCESS_DOWNLOAD;
    }

    public boolean isCancelled(){
        return cancelled;
    }

    public void setCancelled(boolean b){
        cancelled = b;
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param ftpHost     FTP服务器hostname
     * @param ftpUserName 账号
     * @param ftpPassword 密码
     * @param ftpPort     端口
     * @param ftpPath     FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName    ftp文件名称
     * @param input       文件流
     * @return 成功返回true，否则返回false
     */
    public boolean uploadFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort, String ftpPath, String fileName, InputStream input) {
        boolean success = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }
            //ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 使用二进制保存方式
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);
            ftpClient.storeFile(fileName, input);
            input.close();
            ftpClient.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

    //public static String testFile = "ftp://d:d@dygodj8.com:12311/[电影天堂www.dy2018.com]犬之岛HD国英双语中字.mp4";

    public static String getFtpFilePath(String fileName){
        return "/sim/"+fileName.substring(0, 8);
    }

    public static int startDownloadFtpFile(Context context, String ftpFileName,DownloadProgressDialog progressDialog, DownloadCallback callback) {
        final int[] status = {100};
        new Thread(new Runnable() {
            @Override
            public void run() {

                String ftpHost = (String) Utils.get(context, EditActivity.FTP_IP_KEY, EditActivity.FTP_DEFAULT_IP);
                String ftpUserName = (String) Utils.get(context, EditActivity.FTP_USER_KEY, EditActivity.FTP_DEFAULT_USERNAME);
                String ftpPassword = (String) Utils.get(context, EditActivity.FTP_PASSWORD_KEY, EditActivity.FTP_DEFAULT_PASSWORD);
                int ftpPort =(int) Utils.get(context, EditActivity.FTP_PORT_KEY, EditActivity.FTP_DEFAULT_PORT);
                //String ftpPath = (String) Utils.get(context, EditActivity.FTP_PATH_KEY, EditActivity.FTP_DEFAULT_PATH);;
                String ftpPath = getFtpFilePath(ftpFileName);
                Log.d("sgw_d", "FtpUtil run: ftpPath ="+ftpPath);
//                String ftpHost = "10.119.119.67";
//                String ftpUserName = "testftp";
//                String ftpPassword = "1234";
//                String ftpPath = "/home/testftp/test";

//                String ftpHost = "192.168.160.12";
//                String ftpUserName = "sim.zhujing";
//                String ftpPassword = "iopkl";
//                String ftpPath = "/home/disk/code/jetson/data";


                String filename = ftpFileName;
                String sdPath = Utils.getSDPath();   ///+ "/sim"
                String localFilePath = sdPath + "/" + filename;
                File localFile = new File(localFilePath);
                Log.d("sgw_d", "FtpUtil run: sdPath = "+sdPath);
                Log.d("sgw_d", "FtpUtil run: localFilePath ="+localFilePath);
                if (localFile.exists()) {
                    Log.d("sgw_d", "FtpUtil run: 文件已经存在无需下载");
                }else {
                    Log.d("sgw_d", "FtpUtil run: downloadFtpFile");
                    int status = new FtpUtil(context).downloadFtpFileProgress(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, sdPath, filename, progressDialog);
                    callback.updateDownloadImage(status);
                }
            }
        }).start();
        return status[0];
    }


    public static boolean startDownloadFtpFileProgress(String ftpFileName, DownLoadProgressListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ftpHost = "10.119.119.67";
                String ftpUserName = "testftp";
                String ftpPassword = "1234";
                String ftpPath = "/home/testftp/test";
                int ftpPort = 21;
                String filename = ftpFileName;
                String sdPath = Utils.getSDPath(); //+ "/sim";
                String localFilePath = sdPath + "/" + filename;
                File localFile = new File(localFilePath);
                Log.d("sgw_d", "FtpUtil run: sdPath = "+sdPath);
                Log.d("sgw_d", "FtpUtil run: localFilePath ="+localFilePath);
                if (localFile.exists()) {
                    Log.d("sgw_d", "FtpUtil run: 文件已经存在无需下载");
                }else {
                    new FtpUtil().setDownLoadProgressListener(listener).downloadFtpFileProgress(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, sdPath, filename,null);
                }

            }
        }).start();
        return true;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        String ftpHost = "10.119.119.67";
        String ftpUserName = "testftp";
        String ftpPassword = "1234";
        int ftpPort = 21;
        String ftpPath = "/home/testftp/test/1.txt";

        byte[] file_cont = new FtpUtil().getFtpFileBytes(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath);

        System.out.println(new String(file_cont, "UTF-8"));
    }

}

