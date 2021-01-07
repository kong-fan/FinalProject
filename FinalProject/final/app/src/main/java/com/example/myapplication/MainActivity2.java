package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileOutputStream;
import java.io.File;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {
    public Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public static final int CHOOSE_PHOTO=2;
    private Button album;

    private void openAlbum(){  //使用intent來選擇圖片  https://www.jianshu.com/p/67d99a82509b
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  //設置文件類型
        intent.setAction(Intent.ACTION_GET_CONTENT);//讓使用者選擇資料，並返回所選

        /*https://vimsky.com/zh-tw/examples/detail/java-method-android.content.Intent.setType.html*/
        startActivityForResult(intent,CHOOSE_PHOTO);  //a want b to open something (intent, 辨別是哪個Activity回傳的資料，因為我可能一個Activity能夠開啟很多不同的Activity)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this, "獲取權限失敗！請允許檔案授權", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//設定65行功能畫面的結果為RESULT_OK
        super.onActivityResult(requestCode, resultCode, data);//先確定自己被呼叫(requestCode==1)；確定上面結束前設定的功能ok(resultCode == RESULT_OK)；取得intent中的資料(data)
        switch (requestCode) {
            case CHOOSE_PHOTO:
                imageUri =data.getData();   //使用getData方法取得傳回的Intent所包含的URI
                uploadPicture();
                break;
            default:
                break;
        }
    }
    private void uploadPicture() {
        final ProgressDialog pd =new ProgressDialog(this); //顯示上傳進度
        /*http://tw.gitbook.net/android/android_progressbar.html*/
        pd.setTitle("正在上傳圖片，請稍後...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();


        StorageReference riversRef = storageReference.child("images/"+randomKey); //要將圖片上傳到雲端，要先創建對圖片完整路徑的引用

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"上傳成功，等待審核中...",Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"上傳失敗！",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("上傳中： "+ (int)progressPercent + "%");
                    }
                });
    }

    class Data {
        int photo;
    }
    public class MyAdapter extends BaseAdapter {
        private MainActivity2.Data[] data;
        private int view;

        public MyAdapter(MainActivity2.Data[] data, int view) {
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
                    //Toast.makeText(MainActivity2.this, "已儲存", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder saveornot = new AlertDialog.Builder(MainActivity2.this);
                    saveornot.setTitle("通知");
                    saveornot.setMessage("確定要儲存嗎?");
                    saveornot.setNegativeButton("殘忍拒絕", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity2.this, "沒儲存到", Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveornot.setPositiveButton("開心答應", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity2.this, "已儲存", Toast.LENGTH_SHORT).show();
                            BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
                            Bitmap bitmap = draw.getBitmap();
                            FileOutputStream outStream = null;
                            File sdCard = Environment.getExternalStorageDirectory();
                            File dir = new File(sdCard.getAbsolutePath() + "/Pictures");
                            dir.mkdirs();
                            String fileName = String.format("%d.jpg", System.currentTimeMillis());
                            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, String.valueOf(dir));
                        }
                    });
                    saveornot.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity2.this, "取消", Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveornot.show();

                }
            });
            return convertView;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            startActivityForResult(new Intent(MainActivity2.this, MainActivity.class), 1);
        }
        return super.onKeyDown(keyCode, event);
    }

    private Button btn_photo, btn_setting, btn_home,btn_cat,btn_fuck,btn_ha,btn_knowledge,btn_dog
    ,btn_asiaparent,btn_logic,btn_other,btn_math,btn_hi,btn_hell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        TextView textView5 = findViewById(R.id.textView5);
        btn_math=findViewById(R.id.btn_math);
        btn_other=findViewById(R.id.btn_other);
        btn_logic=findViewById(R.id.btn_logic);
        btn_asiaparent=findViewById(R.id.btn_asia_parent);
        btn_hell=findViewById(R.id.btn_hell);
        btn_math=findViewById(R.id.btn_math);
        btn_home = findViewById(R.id.btn_home);
        btn_photo = findViewById(R.id.btn_photo);
        btn_setting = findViewById(R.id.btn_setting);
        btn_cat = findViewById(R.id.btn_cat);
        btn_fuck = findViewById(R.id.btn_fuck);
        btn_ha = findViewById(R.id.btn_ha);
        btn_hi=findViewById(R.id.btn_hi);
        btn_knowledge=findViewById(R.id.btn_knowledge);
        btn_dog=findViewById(R.id.btn_dog);
        album=findViewById(R.id.album);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int vHeight = dm.heightPixels;

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //動態申請對SD卡讀寫的許可權
                if(ContextCompat.checkSelfPermission(MainActivity2.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity2.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity2.this, MainActivity.class), 1);
                finish();
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity2.this, MainActivity3.class), 3);
                finish();
            }
        });


        int[] PhotoIdArray = new int[]{R.drawable.cat_qq_1,R.drawable.cat_qq_2};
        Data[] PhotoData = new Data[PhotoIdArray.length];
        for (int i = 0; i < PhotoData.length; i++) {
            PhotoData[i] = new Data();
            PhotoData[i].photo = PhotoIdArray[i];

            MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

            GridView gridview = findViewById(R.id.gridview);
            gridview.getLayoutParams().height=(int)(vHeight*0.7);
            gridview.setAdapter(PhotoAdapter);
            gridview.setNumColumns(1);
        }
        textView5.setText("貓咪");

        btn_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] PhotoIdArray = new int[]{R.drawable.cat_qq_1,R.drawable.cat_qq_2};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("貓咪");
            }
        });
        btn_ha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] PhotoIdArray = new int[]{R.drawable.hit_me_1,R.drawable.hit_me_2};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("嘲諷");
            }
        });
        btn_fuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] PhotoIdArray = new int[]{R.drawable.fuck_1,R.drawable.fuck_2,R.drawable.fuck_3,R.drawable.fuck_4};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("嗆聲");
            }
        });
        btn_knowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] PhotoIdArray = new int[]{R.drawable.knowledge_1,R.drawable.knowledge_2,R.drawable.knowledge_3};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("學術型");
            }
        });
        btn_dog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] PhotoIdArray = new int[]{R.drawable.dog_1,R.drawable.dog_2,R.drawable.dog_3,R.drawable.dog_4,R.drawable.dog_5
                        ,R.drawable.dog_6,R.drawable.dog_7,R.drawable.dog_8,R.drawable.dog_9,R.drawable.dog_10,R.drawable.dog_11
                ,R.drawable.dog_12,R.drawable.dog_13,R.drawable.dog_14,R.drawable.dog_15,R.drawable.dog_16};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("柴犬");
            }
        });
        btn_asiaparent.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int[] PhotoIdArray = new int[]{R.drawable.asiaparent1,R.drawable.asiaparen2,R.drawable.asiaparent3
                        ,R.drawable.asiaparent4,R.drawable.asiaparent5,R.drawable.asiaparent6,R.drawable.asiaparent7};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("亞洲父母");
            }
        });
        btn_logic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int[] PhotoIdArray = new int[]{R.drawable.logic1,R.drawable.logic2,R.drawable.logic3,R.drawable.logic4,R.drawable.logic5,
                        R.drawable.logic6,R.drawable.logic7,R.drawable.logic8,R.drawable.logic9,R.drawable.logic10,R.drawable.logic11
                        ,R.drawable.logic12,R.drawable.logic13,R.drawable.logic14,R.drawable.logic15,R.drawable.logic16,R.drawable.logic17,
                        R.drawable.logic18,R.drawable.logic19,R.drawable.logic20,R.drawable.logic21,R.drawable.logic22,R.drawable.logic23};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("邏輯");
            }
        });
        btn_other.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int[] PhotoIdArray = new int[]{R.drawable.other1,R.drawable.other2,R.drawable.other3,R.drawable.other4,R.drawable.other5
                        ,R.drawable.other6,R.drawable.other7,R.drawable.other8,R.drawable.other9,R.drawable.other10,R.drawable.other11,
                        R.drawable.other12,R.drawable.other13,R.drawable.other14,R.drawable.other15,R.drawable.other16,R.drawable.other17,
                        R.drawable.other18,R.drawable.other19,R.drawable.other20};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("其他");
            }
        });
        btn_hi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int[] PhotoIdArray = new int[]{R.drawable.goodmorning1,R.drawable.goodmorning2,R.drawable.googmorning3};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("早安");
            }
        });
        btn_hell.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int[] PhotoIdArray = new int[]{R.drawable.hell1,R.drawable.hell2,R.drawable.hell3,R.drawable.hell4};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("地獄");
            }
        });
        btn_math.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int[] PhotoIdArray = new int[]{R.drawable.math1,R.drawable.math2,R.drawable.math3,R.drawable.math4,R.drawable.math5,R.drawable.math6,R.drawable.math7};
                Data[] PhotoData = new Data[PhotoIdArray.length];
                for (int i = 0; i < PhotoData.length; i++) {
                    PhotoData[i] = new Data();
                    PhotoData[i].photo = PhotoIdArray[i];

                    MyAdapter PhotoAdapter = new MyAdapter(PhotoData, R.layout.photo);

                    GridView gridview = findViewById(R.id.gridview);
                    gridview.setAdapter(PhotoAdapter);
                    gridview.setNumColumns(1);
                }
                textView5.setText("數學");
            }
        });
    }

}

