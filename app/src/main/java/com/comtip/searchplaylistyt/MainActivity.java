package com.comtip.searchplaylistyt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //ตัวแปรสำหรับสร้าง url  json query Search
    final String  googleapisSearch = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=";
    String keywords = "";
    String  order = "";
    final String  typePlaylist = "&type=playlist&maxResults=50&key=";
    final String APIkey = "AIzaSyB-iYo9CZ0yb13a4esvDeVOZG2zdTqFf0I";
    String querySearch;


    //ตัวแปรสำหรับสร้าง url json query รับข้อมูลรายชื่อวีดีโอบางส่วนเพื่อสร้าง Preview
    final String  googleapis =  "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=";
    final  String maxResults =  "&maxResults=50&key=";
    String queryPreview;

    // Widgets
    EditText SearchEdit;
    Button SearchBT;
    ListView plList;

    //Array สำหรับแสดงผลลัพธืการ Search
    String [] titelSearch = new String[50];
    String [] playlistSearch = new String[50];

    // ตัวแปรสำหรับส่งข้อมูลไปให้  PlayYoutube
    boolean selectShuffle = false;
    String playlistID = null;
    String headerVideo = null;


    // Favorite
    ArrayList<String> titleFavList = new ArrayList<>();
    ArrayList<String> playlistFavList = new ArrayList<>();
    Button FavBT;
    int indexFav = 14;
    // กำหนดค่า Default  Favorite
    String  saveTitle ="Mylovely Remon✎SLOT 1 JAZZ✎SLOT 2 ROCK✎SLOT 3 COVER✎JackThammarat✎Thailand Hits✎World Hits✎Learn Something New✎Michi Yamamoto✎TED✎NOW LIVE✎MUSIC LIVE✎NEWS LIVE✎SPORTS LIVE✎";
    String  savePlaylist = "FLwaneH6gg3PLNIDCEmhlzoQ✎PLcV3KMryLDO6-EOwC9J4QHuWX4o-Oo0S-✎PLcV3KMryLDO6tAaiWET1UCLZTGpBPsgbf✎PLcV3KMryLDO5Ed8wQhHRJfofA2fApwqLM✎UUBAciNG_R9nqVBk45qKJN6w✎PLdIIlX5liCIrtDfmACBd9ik4kQryWkH6R✎PLrEnWoR732-BHrPp_Pm8_VleD68f9s14-✎PLrEnWoR732-DZV1Jc8bUpVTF_HTPbywpE✎UUR6H89-kkAaEd3D6AgamKVg✎UUAuUUnT6oDeKwE6v1NGQxug✎PLU12uITxBEPHuFM3vOVP-bg51_InK6wKS✎PLFgquLnL59alo82YXbVgoNfcTHZUdyuS9✎PL3ZQ5CpNulQmO9yBytdJhrQMHdBv8RCYY✎PL8fVUTBmJhHJGhVPUWJpDU2aSTEc1bAmu✎";

    //บันทึกก่อนปิด
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    //สถานะ ListView เป็น Favorite หรือ Search
    boolean favPage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupWidgets();
    }

    /**
     *  บันทึกข้อมูลก่อนปิดแอพ
     */
    @Override
    protected void onPause() {
        super.onPause();

         editor.putString("saveTitle",saveTitle);
         editor.putString("savePlaylist",savePlaylist);
         editor.putInt("indexFav",indexFav);
         editor.commit();
    }

    // เตรียม Windgets พื้นฐาน
    public void setupWidgets () {
        SearchBT = (Button) findViewById(R.id.SearchBT);
        SearchEdit = (EditText)findViewById(R.id.SearchEdit);
        plList =  (ListView) findViewById(R.id.plList);

        SearchBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keywords = SearchEdit.getText().toString();
                searchOption();
            }
        });

        FavBT = (Button) findViewById(R.id.FavBT);
        FavBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favList();
            }
        });

        // โหลดข้อมูลล่าสุด
        sp = this.getSharedPreferences("Save Mode", Context.MODE_PRIVATE);
        editor = sp.edit();
        saveTitle = sp.getString("saveTitle","Mylovely Remon✎SLOT 1 JAZZ✎SLOT 2 ROCK✎SLOT 3 COVER✎JackThammarat✎Thailand Hits✎World Hits✎Learn Something New✎Michi Yamamoto✎TED✎NOW LIVE✎MUSIC LIVE✎NEWS LIVE✎SPORTS LIVE✎");
        savePlaylist = sp.getString("savePlaylist","FLwaneH6gg3PLNIDCEmhlzoQ✎PLcV3KMryLDO6-EOwC9J4QHuWX4o-Oo0S-✎PLcV3KMryLDO6tAaiWET1UCLZTGpBPsgbf✎PLcV3KMryLDO5Ed8wQhHRJfofA2fApwqLM✎UUBAciNG_R9nqVBk45qKJN6w✎PLdIIlX5liCIrtDfmACBd9ik4kQryWkH6R✎PLrEnWoR732-BHrPp_Pm8_VleD68f9s14-✎PLrEnWoR732-DZV1Jc8bUpVTF_HTPbywpE✎UUR6H89-kkAaEd3D6AgamKVg✎UUAuUUnT6oDeKwE6v1NGQxug✎PLU12uITxBEPHuFM3vOVP-bg51_InK6wKS✎PLFgquLnL59alo82YXbVgoNfcTHZUdyuS9✎PL3ZQ5CpNulQmO9yBytdJhrQMHdBv8RCYY✎PL8fVUTBmJhHJGhVPUWJpDU2aSTEc1bAmu✎");
        indexFav = sp.getInt("indexFav",14);

        if ((!savePlaylist.isEmpty())&&(!saveTitle.isEmpty())){

            String []  bufferTitltFav = saveTitle.split("\\✎");
            String []  bufferPlaylistFav = savePlaylist.split("\\✎");

            for (int i = 0; i < indexFav;i++){
                titleFavList.add(i,bufferTitltFav[i]);
                playlistFavList.add(i,bufferPlaylistFav[i]);
            }
            favList();
        }

    }


    /*
     Search AsynTask
     */

  private class  SearchPlaylist  extends AsyncTask<Void,String,Void> {

      ProgressDialog pd;
      @Override
      protected void onPreExecute() {
          super.onPreExecute();

          Arrays.fill(titelSearch,null);
          Arrays.fill (playlistSearch,null);

          pd = new ProgressDialog(MainActivity.this);
          pd.setTitle("กำลังค้นหา PlayList : "+keywords);
          pd.setMessage("รอสักครู่ . . .");
          pd.setCancelable(false);
          pd.show();
      }

      @Override
      protected Void doInBackground(Void... params) {

          OkHttpClient okHttpClient = new OkHttpClient();
          Request.Builder builder = new Request.Builder();
          Request request = builder.url(querySearch).build();

          String searchPage = "";

          try {
              Response response = okHttpClient.newCall(request).execute();
              if (response.isSuccessful()) {
                  searchPage = response.body().string();

                  Gson gsonSearch = new Gson();
                  GsonSearchYoutube searchOBJ =  gsonSearch.fromJson(searchPage,GsonSearchYoutube.class);
                  for (int i = 0; i < searchOBJ.getItems().size(); i++) {
                      playlistSearch[i] = searchOBJ.getItems().get(i).getId().getPlaylistId();
                      titelSearch[i] = searchOBJ.getItems().get(i).getSnippet().getTitle();
                  }

              }

          } catch (IOException e) {
              e.printStackTrace();
          }

          return null;
      }


      @Override
      protected void onPostExecute(Void aVoid) {
          super.onPostExecute(aVoid);
          pd.dismiss();

          resultsList();
      }
  }

       //  กำหนด ListView ของหน้า Search
       public void resultsList () {
           CustomList adapter = new CustomList(this,titelSearch);
           plList.setAdapter(adapter);
           plList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
               @Override
               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                   playlistID = playlistSearch[position];
                   headerVideo = titelSearch[position];

                   if (playlistID != null) {
                        favPage = false;
                         new previewPlayList().execute();
                   }
               }

           });
       }

    //  Save Favorite
    public  void favoritePlayList (){
        titleFavList.add(indexFav,headerVideo);
        playlistFavList.add(indexFav,playlistID);
        saveTitle += headerVideo+"✎";
        savePlaylist += playlistID+"✎";
        Toast.makeText(MainActivity.this, "ทำการจัดเก็บ "+titleFavList.get(indexFav)+" เรียบร้อย", Toast.LENGTH_SHORT).show();
        indexFav = indexFav +1 ;
    }

    // ส่งข้อมูลไปให้ PlayYoutube
    public void  intetntYoutube () {
        Intent intent = new Intent(this,PlayYoutube.class);
        intent.putExtra("headerVideo",headerVideo);
        intent.putExtra("playlistID",playlistID);
        intent.putExtra("selectShuffle",selectShuffle);
        startActivity(intent);
    }

    // กดปุ่ม Back แล้วปิด Activity ทั้งหมด
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    //  กำหนด ListView ของหน้า Favorite
    public void favList(){
        CustomArrayList  adapter = new CustomArrayList(this,titleFavList);
        plList.setAdapter(adapter);
        plList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               playlistID = playlistFavList.get(position);
                headerVideo = titleFavList.get(position);

                if (playlistID != null) {
                    favPage = true;
                    new previewPlayList().execute();
                }
            }
        });
    }

    /*
      Preview AsyncTask
     */

    private class  previewPlayList  extends  AsyncTask<Void,Void,Void> {
       String playlistPage = "";
       ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            queryPreview = googleapis+playlistID+maxResults+APIkey;

            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("กำลังสร้าง Preview Playlist ของ "+headerVideo);
            pd.setMessage("รอสักครู่");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(queryPreview).build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful())  {
                    playlistPage =  response.body().string();

                    // เข้ากระบวนการใช้ Gson  ดึง Playlist ทั้งหมด
                    Gson gsonYoutube = new Gson();
                    GsonGetYoutube obj =  gsonYoutube.fromJson(playlistPage,GsonGetYoutube.class);
                    playlistPage = "✧✦✧ มีทั้งหมด "+obj.getPageInfo().getTotalResults()+" รายการ\n " +
                            "!!! คำเตือน : Shuffle อาจใช้เวลาโหลด ขึ้นอยู่กับจำนวนรายการ \n"+
                            "*** หมายเหตุ : Preview นี้แสดงผลแค่ 50 รายการแรก \n"+
                    "____________________________________________________________\n \n";


                    //  ใส่ข้อมูลชื่อวีดีโอและรหัสวีดีโอ
                    for (int i = 0; i < obj.getItems().size(); i++) {
                        playlistPage += obj.getItems().get(i).getSnippet().getTitle()+"\n";
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
            previewShowPlaylist(playlistPage);
        }
    }

    //   แสดง Preview รายฃื่อวีดีโอใน Playlist นั้นๆ
    public  void  previewShowPlaylist (String playlistPage) {

        TextView content = new TextView(MainActivity.this);
        content.setMovementMethod(new ScrollingMovementMethod());
        content.setText(playlistPage);
        AlertDialog.Builder alertDB = new AlertDialog.Builder(MainActivity.this);
        alertDB.setTitle("ต้องการเล่น "+headerVideo+" แบบไหน ? ");
        alertDB.setView(content);

        alertDB.setNegativeButton("✧ Normal ✧", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // normal
                selectShuffle = false;
                intetntYoutube();
            }
        });

        alertDB.setPositiveButton("✦ Shuffle ✦" ,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // shuffle
                selectShuffle = true;
                intetntYoutube();
            }
        });


         if (favPage) {
             //กรณีอยู่หน้า Favorite
             alertDB.setNeutralButton("✦ Delete ✦", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     AlertDialog.Builder alertDeletee = new AlertDialog.Builder(MainActivity.this);
                     alertDeletee.setTitle("ต้องการลบ " + headerVideo + " ใช่ไหม ? ");
                     alertDeletee.setPositiveButton("✔ Yes ✔", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             // Deleter Array Row
                             savePlaylist = savePlaylist.replace(playlistID + "✎", "");
                             saveTitle = saveTitle.replace(headerVideo + "✎", "");

                             if (indexFav != 0) {
                                 indexFav = indexFav - 1;
                             }

                             recreate(); // สั่ง Recrate Activity ทั้งหมดตั้งแต่ต้น
                         }
                     });


                     alertDeletee.setNegativeButton("✘ No ✘", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             //no Action
                         }
                     });

                     AlertDialog alertD = alertDeletee.create();
                     alertD.show();
                 }
             });
         }else{
               // กรณีอยู่หน้า Search  ปุ่มกลางจะเปลี่ยนจาก Delete เป็น Save
             alertDB.setNeutralButton("✦ Save Favorite ✦", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     favoritePlayList();
                 }
             });
         }
        AlertDialog alert = alertDB.create();
        alert.show();

    }

    /*
       Option ของปุ่ม Search
     */

    public void searchOption () {
        AlertDialog.Builder alertDB = new AlertDialog.Builder(MainActivity.this);
        alertDB.setTitle("ค้นหา "+keywords+" ให้แสดงผลลัพธ์จัดเรียงตาม ? ");

        alertDB.setNegativeButton("✎ ใหม่ล่าสุด ✎", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                order="&order=date";
                querySearch = googleapisSearch+keywords+order+typePlaylist+APIkey;
                new SearchPlaylist().execute();

            }
        });

        alertDB.setNeutralButton("✎ จำนวนวิดีโอ ✎", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                order="&order=videoCount";
                querySearch = googleapisSearch+keywords+order+typePlaylist+APIkey;
                new SearchPlaylist().execute();
            }
        });

        alertDB.setPositiveButton("✎ ปกติ ✎", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                order="";
                querySearch = googleapisSearch+keywords+order+typePlaylist+APIkey;
                new SearchPlaylist().execute();
            }
        });

        AlertDialog alertO = alertDB.create();
        alertO.show();
    }


}
