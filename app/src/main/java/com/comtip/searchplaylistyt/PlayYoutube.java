package com.comtip.searchplaylistyt;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by TipRayong on 12/7/2559.
 */
public class PlayYoutube extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    final String YOUTUBE_API_KEY = "AIzaSyB-iYo9CZ0yb13a4esvDeVOZG2zdTqFf0I";
    YouTubePlayerView youtubeView;

    String  header  = null;
    boolean selectShuffle = false;

    TextView headerVideo;
    Button   finishBT;

    // ตัวแปรสำหรับดึง Playlist เพื่อสร้าง Shuffle
    String playlistID = null;
    ArrayList<String> shuffle = new ArrayList<>();
    final String  googleapis =  "https://www.googleapis.com/youtube/v3/playlistItems?";
    String  pageToken = "";
    String  pageTokenBuffer ="YouTUBE";
    final String snippet = "part=snippet&playlistId=";
    final  String maxResults =  "&maxResults=50&key=";
    String queryYTPL;
    String playlistPage;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.youtube_layout);
        youtubeView = (YouTubePlayerView) findViewById(R.id.youtubeView);
        headerVideo = (TextView) findViewById(R.id.headerVideo);
        finishBT = (Button) findViewById(R.id.finishBT);

        finishBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle  bundle  =  getIntent().getExtras();
        header = bundle.getString("headerVideo",null);
        selectShuffle = bundle.getBoolean("selectShuffle",false);
        headerVideo.setText(header);
        playlistID = bundle.getString("playlistID",null);

        if (playlistID != null) {

            if (selectShuffle) {

                new GetYoutubePlaylistAllPage().execute();
            }
            else {
                    youtubeView.initialize(YOUTUBE_API_KEY, this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
          youTubePlayer.setFullscreen(true);
          if(selectShuffle) {
              youTubePlayer.loadVideos(shuffle);
          }else {
              youTubePlayer.loadPlaylist(playlistID);
          }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, 1).show();
        } else {
            Toast.makeText(this, "Unknow Error", Toast.LENGTH_LONG).show();
        }
    }


    // โหลดข้อมูล  Playlist จาก Youtube  จะมีกี่ Page  กี่ Video  มีเท่าไรดึงได้หมด

    private class  GetYoutubePlaylistAllPage  extends AsyncTask<Void,String,Void> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pageTokenBuffer = "YOUTUBE";
            pd = new ProgressDialog(PlayYoutube.this);
            pd.setTitle("กำลังทำการ Shuffle ใน "+header);
            pd.setMessage("รอสักครู่ . . .");
            pd.setCancelable(false);
            pd.show();

        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // บอกสถานะชื่อวีดีโอที่กำลังโหลด
            pd.setMessage(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (pageTokenBuffer != null) {

                queryYTPL = googleapis + pageToken + snippet + playlistID + maxResults +YOUTUBE_API_KEY;

                OkHttpClient okHttpClient = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(queryYTPL).build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful())  {
                        playlistPage =  response.body().string();

                        // เข้ากระบวนการใช้ Gson  ดึง Playlist ทั้งหมด
                        Gson gsonYoutube = new Gson();
                        GsonGetYoutube obj =  gsonYoutube.fromJson(playlistPage,GsonGetYoutube.class);

                        //  ใส่ค่าตัวแปรให้ pageToken  สำหรับใช้ในการดึงข้อมูลหน้าต่อไป
                        pageTokenBuffer = obj.getNextPageToken();
                        if(pageTokenBuffer != null) {
                            pageToken = "pageToken=" + pageTokenBuffer + "&";
                        }

                        //  ใส่ข้อมูลชื่อวีดีโอและรหัสวีดีโอ
                        for (int i = 0; i < obj.getItems().size(); i++) {
                            shuffle.add(obj.getItems().get(i).getSnippet().getResourceId().getVideoId());
                            // บอกสถานะชื่อวีดีโอที่กำลังโหลด
                            publishProgress(obj.getItems().get(i).getSnippet().getTitle());
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();

            // shuffle แล้วส่งข้อมูลไปให้หน้า youtube เล่นทันที
            Collections.shuffle(shuffle);
            youtubeView.initialize(YOUTUBE_API_KEY, PlayYoutube.this);

        }
    }
}
