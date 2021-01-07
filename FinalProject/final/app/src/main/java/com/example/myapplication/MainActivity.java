package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private Button btn_photo,btn_setting,btn_home;

    class Data {
        int photo;
    }
    public class MyAdapter extends BaseAdapter {
        private MainActivity.Data[] data;
        private int view;

        public MyAdapter(MainActivity.Data[] data, int view) {
            this.data = data;
            this.view = view;
        }

        public int getCount() {
            return data.length;
        }

        public Object getItem(int position) {
            return data[position];
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(view, parent, false);
            ImageView imageView = convertView.findViewById(R.id.imageView);
            imageView.setImageResource(data[position].photo);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(MainActivity.this, "已儲存", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder saveornot = new AlertDialog.Builder(MainActivity.this);
                    saveornot.setTitle("通知");
                    saveornot.setMessage("確定要儲存嗎?");
                    saveornot.setNegativeButton("殘忍拒絕", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "沒儲存到", Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveornot.setPositiveButton("開心答應", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "已儲存", Toast.LENGTH_SHORT).show();

                            //Drawable----->Bitmap
                            BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
                            Bitmap bitmap = draw.getBitmap();


                            String fileName = String.format("%d.jpg", System.currentTimeMillis()); //檔案類型
                            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName,"description"); //儲存到相簿
                        }
                    });
                    saveornot.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveornot.show();
                }
            });
            return convertView;
        }
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程式", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        btn_home=findViewById(R.id.btn_home);
        btn_photo=findViewById(R.id.btn_photo);
        btn_setting=findViewById(R.id.btn_setting);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int vHeight = dm.heightPixels;

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this,MainActivity2.class),2);
                finish();
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this,MainActivity3.class),3);
                finish();
            }
        });

        int[] PhotoIdArray = new int[]{R.drawable.cat_qq_1,R.drawable.cat_qq_2,R.drawable.cat_qq_1,R.drawable.cat_qq_2,
                R.drawable.hit_me_1,R.drawable.hit_me_2,R.drawable.fuck_1,R.drawable.fuck_2,R.drawable.fuck_3,R.drawable.fuck_4,
                R.drawable.knowledge_1,R.drawable.knowledge_2,R.drawable.knowledge_3,R.drawable.dog_1,R.drawable.dog_2,
                R.drawable.dog_3,R.drawable.dog_4,R.drawable.dog_5, R.drawable.dog_6,R.drawable.dog_7,R.drawable.dog_8,
                R.drawable.dog_9,R.drawable.dog_10,R.drawable.dog_11, R.drawable.dog_12,R.drawable.dog_13,R.drawable.dog_14,
                R.drawable.dog_15,R.drawable.dog_16,R.drawable.asiaparent1,R.drawable.asiaparen2,R.drawable.asiaparent3
                ,R.drawable.asiaparent4,R.drawable.asiaparent5,R.drawable.asiaparent6,R.drawable.asiaparent7,R.drawable.logic1,R.drawable.logic2,R.drawable.logic3,R.drawable.logic4,R.drawable.logic5,
                R.drawable.logic6,R.drawable.logic7,R.drawable.logic8,R.drawable.logic9,R.drawable.logic10,R.drawable.logic11
                ,R.drawable.logic12,R.drawable.logic13,R.drawable.logic14,R.drawable.logic15,R.drawable.logic16,R.drawable.logic17,
                R.drawable.logic18,R.drawable.logic19,R.drawable.logic20,R.drawable.logic21,R.drawable.logic22,R.drawable.logic23,R.drawable.other1,R.drawable.other2,R.drawable.other3,R.drawable.other4,R.drawable.other5
                ,R.drawable.other6,R.drawable.other7,R.drawable.other8,R.drawable.other9,R.drawable.other10,R.drawable.other11,
                R.drawable.other12,R.drawable.other13,R.drawable.other14,R.drawable.other15,R.drawable.other16,R.drawable.other17,
                R.drawable.other18,R.drawable.other19,R.drawable.other20,R.drawable.goodmorning1,R.drawable.goodmorning2,R.drawable.googmorning3
        ,R.drawable.hell1,R.drawable.hell2,R.drawable.hell3,R.drawable.hell4};

        Data[] PhotoData = new Data[3];
        for (int i = 0; i < 3; i++) {
            int a = (int)(Math.random()* PhotoIdArray.length);
            PhotoData[i] = new Data();
            PhotoData[i].photo = PhotoIdArray[a];

            MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

            GridView gridview = findViewById(R.id.gridview);
            gridview.getLayoutParams().height=(int)(vHeight*0.35);
            gridview.setAdapter(PhotoAdapter);
            gridview.setNumColumns(1);
        }

        Data[] PhotoData2 = new Data[3];
        for (int i = 0; i < 3; i++) {
            int a = (int)(PhotoIdArray.length - i - 1);
            PhotoData2[i] = new Data();
            PhotoData2[i].photo = PhotoIdArray[a];

            MyAdapter PhotoAdapter = new MyAdapter(PhotoData2, R.layout.photo);

            GridView gridview2 = findViewById(R.id.gridview2);
            gridview2.getLayoutParams().height=(int)(vHeight*0.35);
            gridview2.setAdapter(PhotoAdapter);
            gridview2.setNumColumns(1);
        }
    }
}