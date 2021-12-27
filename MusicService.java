package com.jal.www.jalmusic;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {
    static MediaPlayer mlastPlayer;
    static int mPosition;
    private int position=0;
    private String path = "";
    private String TAG = "MusicServiceLog";
    private MediaPlayer player;
    private Music music;
    private DownloadTask downloadTask;
    String url=" http://m7.music.126.net/20211214215515/987a23046b9ffdb2f6dfbbbc64526a45/ymusic/f114/889b/4cb6/5aa1f9398d3731587b3dd64d8e8c4040.mp3";


    private ArrayList<Music>listMusic;
    private Context context;
    private RemoteViews remoteView;
    private Notification notification;
    private String notificationChannelID = "1";
    public static String ACTION = "to_service";
    public static String KEY_USR_ACTION = "key_usr_action";
    public static final int ACTION_PRE = 0, ACTION_PLAY_PAUSE = 1, ACTION_NEXT = 2;
    public static String MAIN_UPDATE_UI = "main_activity_update_ui";  //Action
    public static String KEY_MAIN_ACTIVITY_UI_BTN = "main_activity_ui_btn_key";
    public static String KEY_MAIN_ACTIVITY_UI_TEXT = "main_activity_ui_text_key";
    public static final int  VAL_UPDATE_UI_PLAY = 1,VAL_UPDATE_UI_PAUSE =2;
    private int notifyId = 1;

    @Override
    public IBinder onBind(Intent intent) {

        //When onCreate() is executed, onBind() will be executed to return the method of operating the music.
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        listMusic = MusicList.getMusicData(context);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        position = bundle.getInt("position");
        if (mlastPlayer == null || mPosition != position){
            prepare();
        }else{
            player = mlastPlayer;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    void prepare(){
        music = listMusic.get(position);
        path = music.getUrl();
        Log.i(TAG,"path:"+path);
        player = new MediaPlayer();//This is only done once, used to prepare the player.
        if (mlastPlayer !=null){
            mlastPlayer.stop();
            mlastPlayer.release();
        }
        mlastPlayer = player;
        mPosition = position;
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Log.i(TAG,path);
            player.setDataSource(path); //Prepare resources
            player.prepare();
            player.start();
            Log.i(TAG, "Ready to play music");
        } catch (IOException e) {
            Log.i(TAG,"ERROR");
            e.printStackTrace();
        }

    }





    //This method contains operations on music
    public class MyBinder extends Binder {

        public boolean isPlaying(){
            return player.isPlaying();
        }

        public void play() {
            if (player.isPlaying()) {
                player.pause();
                Log.i(TAG, "Play stop");
            } else {
                player.start();
                Log.i(TAG, "Play start");
            }
        }

        //Play the next music
        public void next(int type){
            position +=type;
            position = (position + listMusic.size())%listMusic.size();
            music = listMusic.get(position);
            prepare();
        }

        public void down(){
            //Log.e("Download Url:",url);
            downloadTask = new DownloadTask();
            downloadTask.execute();
            Toast.makeText(MusicService.this, "Start Download", Toast.LENGTH_SHORT).show();

        }

        //Returns the length of the music in milliseconds
        public int getDuration(){
            if(player!=null) {
                return player.getDuration();
            }
            else{
                return 0;
            }
        }

        //Return the name of the music
        public String getName(){
            return music.getName();
        }

        //Returns the current progress of the music in milliseconds
        public int getCurrenPostion(){
            return player.getCurrentPosition();
        }

        //Set the progress of music playback in milliseconds
        public void seekTo(int mesc){
            player.seekTo(mesc);
        }
    }
}