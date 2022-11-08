package com.dam.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    public static final int PERMISSION_READ = 0;

    RecyclerView recyclerView;
    TextView tvSongTitle, tvCurrentPos, tvTotalDuration;
    ImageButton btnPrev, btnPlay, btnNext;
    SeekBar sbPosition;

    MediaPlayer mediaPlayer;
    ArrayList<ModelSong> songArrayList;
    int currentArrayPos = 0;
    long currentPos, totalDuration;

    private void initUI(){
        recyclerView = findViewById(R.id.rvRecycler);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvCurrentPos = findViewById(R.id.tvCurrentPos);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        sbPosition = findViewById(R.id.sbPosition);

        mediaPlayer = new MediaPlayer();

        songArrayList = new ArrayList<>();

        // init du Recycler
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL, false)
        );
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    // Vérification des accèes aux données sur le stockage externe (sdk32)
    public boolean checkPermission(){
        int READ_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (READ_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_SHORT).show();
                    } else {
                        setSong();
                    }
                    // Toast.makeText(this, getApplicationContext().getExternalFilesDir(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setSong() {
        getAudioFiles();
    }

    private void playSong(int pos){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, songArrayList.get(pos).getSongUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.ic_pause_white_48);
            tvSongTitle.setText(songArrayList.get(pos).getSongArtist() + " - " + songArrayList.get(pos).getSongTitle());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSongProgress();
    }

    private void setSongProgress() {
        currentPos = mediaPlayer.getCurrentPosition();
        totalDuration = mediaPlayer.getDuration();
        tvCurrentPos.setText(timerConversion(currentPos));
        tvTotalDuration.setText((timerConversion(totalDuration)));
        sbPosition.setMax((int) totalDuration);
        final Handler handle = new Handler();

        Runnable runnable = new Runnable() {
           @Override
            public void run() {
               try {
                    currentPos = mediaPlayer.getCurrentPosition();
                    tvCurrentPos.setText(timerConversion(currentPos));
                    sbPosition.setProgress((int) currentPos);
                    handle.postDelayed(this, 1000);
               } catch (IllegalStateException e) {
                   e.printStackTrace();
               }
            }
        };
        handle.postDelayed(runnable, 1000);
    }

    public String timerConversion(long value){ // duree en ms
        String songDuration="";
//        int duree = (int)  value;
        long duree = value;
        int hrs = (int) (duree / (3600 * 1000));
        int min = (int) ((duree / 60000) % 60000);
        int sec = (int) (duree % 60000 / 1000);

        if (hrs>0){
            songDuration = String.format("%02d:%02d:%02d", hrs, min, sec);
        }
        else {
            songDuration = String.format("%02d:%02d", min, sec);
        }
        return songDuration;
    }

    public void getAudioFiles(){
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri coverFolder = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(coverFolder, albumId);
                Log.i("DEBUG", title + " : " + albumArtUri);

                // remplaissage row
                ModelSong modelSong = new ModelSong();
                modelSong.setSongTitle(title);
                modelSong.setSongArtist(artist);
                modelSong.setSongUri(Uri.parse(url));
                modelSong.setSongDuration(duration);
                modelSong.setSongCover(albumArtUri);

                songArrayList.add(modelSong);

            } while (cursor.moveToNext());
        }

        AdapterSong adapterSong = new AdapterSong(this, songArrayList);
        recyclerView.setAdapter(adapterSong);
        adapterSong.setOnItemClickListener((new AdapterSong.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                playSong(pos);
            }
        }));
/*
        adapterSong.setOnItemClickListener((new AdapterSong.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                playSong(pos);
            }
        }));
*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        if (checkPermission()){
            setSong();
        }


    }
}