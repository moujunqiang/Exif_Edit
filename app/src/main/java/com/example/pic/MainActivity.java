package com.example.pic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button openpic;
    private String path;
    private ImageView img_show;
    private List list;
    private RecyclerView recyclerView;
    private MsgAdapter adapter;
    private GetExfi getExfi;
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //请求权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        //按钮点击动作
        openpic = (Button)findViewById(R.id.open_pic);
        img_show = (ImageView)findViewById(R.id.img_show);
        getExfi = new GetExfi();
        openpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        });
    }

    //编写Intent返回后的逻辑
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("openpic", "onActivityResult: first_OK requestCode："+requestCode+" resultCode:"+resultCode);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    try {
                        //Toast.makeText(MainActivity.this,"返回",Toast.LENGTH_SHORT).show();
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        path = cursor.getString(columnIndex);  //获取照片路径
                        cursor.close();
                        Toast.makeText(MainActivity.this,"获取到图片路径："+path,Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        img_show.setImageBitmap(bitmap);
                        list=getExfi.getExfi(path);
                        /*
                        显示信息
                         */
                        recyclerView=(RecyclerView)findViewById(R.id.msg_RecyclerView);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                        recyclerView.setLayoutManager(layoutManager);
                        adapter=  new MsgAdapter(list);
                        recyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListener(new MsgAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(RecyclerView parent, View view, int position, Msg data) {
                                Toast.makeText(MainActivity.this,data.getTitle()+":"+data.getMsg(),Toast.LENGTH_LONG).show();
                                showCustomizeDialog(data.getType(),data.getMsg());
                            }
                        });
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    //Dialog
    private void showCustomizeDialog(final int type,final String mag_data) {
        /* @setView 装入自定义View ==> R.layout.dialog_customize
         * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
         * dialog_customize.xml可自定义更复杂的View
         */
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.modify,null);
        customizeDialog.setTitle("请输入修改后的值");
        customizeDialog.setView(dialogView);
        TextView textView = (TextView)dialogView.findViewById(R.id.modify_edit);
        textView.setText(mag_data);
        Toast.makeText(MainActivity.this,"请严格按照原有格式规范填写，否则可能导致软件崩溃或图片损坏！",Toast.LENGTH_LONG).show();
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText edit_text =
                                (EditText) dialogView.findViewById(R.id.modify_edit);
                        //String newdata=edit_text.
                        getExfi.setexif(type,path,edit_text.getText().toString());
                        list=getExfi.getExfi(path);
                        adapter.setMsgList(list);
                        recyclerView.setAdapter(adapter);
                        Log.d("setexfi", "final");
                    }
                });
        customizeDialog.show();
    }

    //首页右上角的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_about:
                Intent intent=new Intent(MainActivity.this,AboutActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}