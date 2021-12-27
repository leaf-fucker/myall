package com.jal.www.jalmusic;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 参数1：任务字符串参数
 * 参数2：进度显示单位
 * 参数3：反馈执行结果
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;



    public DownloadTask(){
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream inputStream = null;
        RandomAccessFile saveFile = null;
        File file;

        // 用于记录文件长度
        long downloadedLength = 0;
        // 两个参数，第一个是要下载歌曲的url，第二个是对应要保存的文件名
        String downloadUrl = "https://webfs.ali.kugou.com/202112160953/5ad21aaa1acbf5a385157b485e7de7f6/G122/M02/0C/15/GocBAFoOU6iARKjGADvQA5F_zqE188.mp3";
        String fileName = "qweqweqweqweqweqwhheqw.mp3";

        // 将文件保存到SD卡download目录下
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        Log.e("The directory is:", directory);

        file = new File(directory + "/" + fileName);
        Log.e("The file path is", file.getPath());

        // 检查文件是否存在，如果存在，从已有长度
        if(file.exists()){
            downloadedLength = file.length();
        }
        Log.e("The file path is", file.getPath());
        // 调用获取要下载文件的总长度
        long contentLength = getContentLength(downloadUrl);
        Log.e("The contentLength is", String.valueOf(contentLength));
        // 检查文件长度，如果为0，表示文件有问题，返回失败编号
        if(contentLength == 0){
            return FAILED;
        }


        // 如果当前文件长度等于待下载文件长度，则表示已经下载
        else if(contentLength == downloadedLength){
            System.out.println("successssss");
            return SUCCESS;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("RANGE","bytes= "+downloadedLength+"-")
                .url(downloadUrl).build();
        try {
            Log.d("tagxxx", downloadUrl);
            Response response = client.newCall(request).execute();
            Log.d("tag", "win success");
            inputStream = Objects.requireNonNull(response.body()).byteStream();
            saveFile = new RandomAccessFile(file, "rw");
            // 跳过已下载的字节
            saveFile.seek(downloadedLength);

            byte[] buffer = new byte[1024];
            int total = 0;
            int length;

            while((length = inputStream.read(buffer)) != -1){
                total += length;
                // 写入文件
                saveFile.write(buffer, 0, length);
                // 计算下载百分比并显示
                int progress = (int)((total + downloadedLength) * 100 / contentLength);
                // 发布进度单位
                publishProgress(progress);

            }
            Objects.requireNonNull(response.body()).close();
            Log.d("tag", "download success");
            return SUCCESS;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(inputStream != null){
                    inputStream.close();
                }
                if(saveFile != null){
                    saveFile.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return FAILED;
    }




    /**
     *  返回最终的下载状态
     * @param status 下载状态
     */


    /**
     * 获取要下载的文件长度
     * @param downloadUrl 要下载文件的url
     * @return 文件长度
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private long getContentLength(String downloadUrl)  {
        long currentLength = 0;
        OkHttpClient client;
        Request request;
        Response response;
        try{
            client = new OkHttpClient();
            request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
            response = client.newCall(request).execute();
            // 如果请求成功接受，获取文件长度
            if(response.isSuccessful()){
                currentLength = Objects.requireNonNull(response.body()).contentLength();
                Objects.requireNonNull(response.body()).close();
            }
            return currentLength;
        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }


}
