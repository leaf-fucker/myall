package com.jal.www.jalmusic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

public class MusicList {
    public static ArrayList<Music> getMusicData(Context context) {
        ArrayList<Music> musicList = new ArrayList<Music>();
        ContentResolver cr = context.getContentResolver();
        if (cr != null) {
            // Get all the music
            Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (null == cursor) {
                return null;
            }
            if (cursor.moveToFirst()) {
                do {
                    Music m = new Music();
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if ("<unknown>".equals(singer)) {
                        singer = "未知艺术家";
                    }
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String sbr = name.substring(name.length() - 3, name.length());
                    // Log.e("--------------", sbr);
                    if (sbr.equals("mp3")) {
                        m.setTitle(title);
                        m.setSinger(singer);
                        m.setAlbum(album);
                        m.setSize(size);
                        m.setTime(time);
                        m.setUrl(url);
                        m.setName(name);

                        musicList.add(m);
                    }
                } while (cursor.moveToNext());
            }
        }
        return musicList;
    }
}

