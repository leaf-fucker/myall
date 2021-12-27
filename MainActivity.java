package com.jal.www.jalmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView listView;
    private MyConnection conn;
    private String TAG = "DetailsActivity";
    private Button btn_pre;
    private Button btn_play;
    private Button btn_next;
    private Button btn_down;
    private ImageView btn_return;
    private SeekBar seekBar;
//    private MusicButton imageView;
    private TextView tv_title,tv_cur_time,tv_total_time;
    private MusicService.MyBinder musicControl;
    private static final int UPDATE_UI = 0;
    private ArrayList<Music> listMusic;

//    MyReceiver myReceiver;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI:
                    updateUI();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listMusic = MusicList.getMusicData(this);
        Intent intent = new Intent(this, MusicService.class);
        Bundle bundle =  new Bundle();
        intent.putExtras(bundle);
        conn = new MyConnection();
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
        bindViews();
        requestPermission();
        //Mixed mode binding service
    }


    private void bindViews() {
        btn_pre = findViewById(R.id.btn_pre);
        btn_play = findViewById(R.id.btn_play);
        btn_next = findViewById(R.id.btn_next);
        btn_down = findViewById(R.id.down);

        seekBar =  findViewById(R.id.sb);

        tv_cur_time =findViewById(R.id.tv_cur_time);
        tv_total_time = findViewById(R.id.tv_total_time);

        btn_pre.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        btn_down.setOnClickListener(this);
        listView = this.findViewById(R.id.listView1);
        listMusic = MusicList.getMusicData(getApplicationContext());
//        Log.i(TAG, "listMusic.size()=="+listMusic.size());
        MusicAdapter adapter = new MusicAdapter(this, listMusic);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Progress bar change
                if (fromUser) {
                    musicControl.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Start touching the progress bar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Stop touching the progress bar
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                play(v);
                break;
            case R.id.btn_next:
                next(v);
                break;
            case R.id.btn_pre:
                pre(v);
                break;
            case R.id.down:
                down(v);
                break;
        }
    }

    private class MyConnection implements ServiceConnection {

        //This method will be entered after the service is started.
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "::MyConnection::onServiceConnected");
            //Get MyBinder in service
            musicControl = (MusicService.MyBinder) service;
            //Update button text
            updatePlayText();
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "::MyConnection::onServiceDisconnected");

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unbind from the service after exiting
        unbindService(conn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop the progress of the update progress bar
        handler.removeCallbacksAndMessages(null);
    }

    //Update progress bar
    private void updateProgress() {
        int currenPostion = musicControl.getCurrenPostion();
        seekBar.setProgress(currenPostion);
    }


    //Update button text
    public void updatePlayText() {
        if(MusicService.mlastPlayer!=null &&MusicService.mlastPlayer.isPlaying()){
            btn_play.setText(R.string.pause);
        }else{

            btn_play.setText(R.string.play);
        }
    }

    public void play(View view) {
;
        musicControl.play();
        updatePlayText();
    }

    public void next(View view) {

        musicControl.next(1);
        updatePlayText();
    }

    public void pre(View view) {

        musicControl.next(-1);
        updatePlayText();
    }
    public void down(View view) {

        musicControl.down();

    }

    public void updateUI(){

        //Set the maximum value of the progress bar
        int cur_time = musicControl.getCurrenPostion(), total_time = musicControl.getDuration();
        seekBar.setMax(total_time);
        //Set the progress of the progress bar
        seekBar.setProgress(cur_time);
        String str = musicControl.getName();
        tv_cur_time.setText(timeToString(cur_time));
        tv_total_time.setText(timeToString(total_time));

        updateProgress();
        //Update the UI bar every 500 milliseconds using Handler
        handler.sendEmptyMessageDelayed(UPDATE_UI, 500);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++) {

                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED){
                            String s = permissions[i];
                            Toast.makeText(this,s+"权限被拒绝了",Toast.LENGTH_SHORT).show();
                        }else{
                            bindViews();
//                            initView();

                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    private void requestPermission(){

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()){
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),1);
        }else {
            bindViews();
        }
    }

    private String timeToString(int time) {
        time /= 1000;
        return String.format("%02d:%02d",time/60,time%60);
    }
}
