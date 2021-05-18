package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicActivity extends AppCompatActivity {

    private MediaPlayer mp = new MediaPlayer(); //播放器  和界面无关系的变量可以直接初始化
    private String song_path = ""; //音乐路径，初始为空串
    private int currIndex = 0; // 表示当前播放的音乐索引
    private ArrayList<MusicBean> list = new ArrayList<MusicBean>(); //音乐列表，存放音乐路径
    ListView listView; // ListView 控件
    MusicAdapter adapter;//listview控件的adapter
    SeekBar seekBar ;
    private boolean isChanging=false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
    Timer timer;
    TimerTask timerTask;
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");//设定时间格式
    boolean flag=false;
    int currentPosition;
    int ModelChang=1;//播放模式

    public void setListui() {
        MusicAdapter musicAdapter=new MusicAdapter(
                getApplicationContext(),
                R.layout.item_music,
                list);
        listView=(ListView)findViewById(R.id.songList);
        listView.setAdapter(musicAdapter);
        Log.d("TAG", "setlist++1");

    }


    public void Init(){
        //申请权限
        if (ActivityCompat.checkSelfPermission( MusicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) { //如果没有获得授权，则申请权限
            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
            return;
        }

        //填充list数据
        int id;
        String song;
        String singer;
        long duration;
        String time;



        //文件初始化
        File sdpath= Environment.getExternalStorageDirectory(); //获得sdcard路径
        File path=new File(sdpath+"//mp3//"); //获得sdcard的mp3文件夹
        //返回以.mp3结尾的文件 ( MyFilter类代码见后页 )
        File[ ] songFiles = path.listFiles( new MyFilter(".mp3") );
        id=0;
        for (File file :songFiles){
            id++;
            song=getFileName(file.getAbsolutePath());
            singer=getFileName(file.getAbsolutePath());
            Log.d("TAG", file.getAbsolutePath());
            mp = new MediaPlayer();
            try {
                mp.setDataSource(file.getAbsolutePath());
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            duration= mp.getDuration();
            time=sdf.format(new Date(duration));
            MusicBean musicBean=new MusicBean(Integer.toString(id),song,singer,file.getAbsolutePath(), time);//整合数据
            list.add(musicBean); //获取文件的绝对路径
        }

        //设置listview
        setListui();
        registerForContextMenu(listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); //单选
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currIndex = position;
                ImageButton btn = (ImageButton) findViewById(R.id.con_play);
                btn.setImageDrawable(getResources().getDrawable(R.mipmap.music_pause));
                start(); //自定义播放函数，见下页
            }
        });

        //长按监听
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currIndex = position;
                return false;
            }
        });

        //进度条
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekbar());



        //按键初始化
        final ImageButton btn_play = (ImageButton) findViewById(R.id.con_play);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( song_path.isEmpty() ) { //初始从第一首开始播放
                    currIndex = 0;
                    btn_play.setImageDrawable(getResources().getDrawable(R.mipmap.music_pause));
                    start();
                }
                if ( !mp.isPlaying() ) { //如果暂停
                    btn_play.setImageDrawable(getResources().getDrawable(R.mipmap.music_pause));
                   if(flag){
                       flag=false;
                       mp.start();
                   }
                    else
                    start();
                } else {
                    flag=true;
                    currentPosition = mp.getCurrentPosition();
                    btn_play.setImageDrawable(getResources().getDrawable(R.mipmap.music_play_arrow));
                    mp.pause();
                } }
        });


        final ImageButton btn_next = (ImageButton) findViewById(R.id.con_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( currIndex + 1 < list.size() ) {
                    currIndex++;
                    start();
                } else {
                    Toast.makeText(getApplicationContext(), "已经是最后一首歌曲了", Toast.LENGTH_SHORT).show();
                } }
        });

        final ImageButton btn_prev = (ImageButton) findViewById(R.id.con_pre);
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (currIndex - 1) >= 0 ) {
                    currIndex--;
                    start();
                } else {
                    Toast.makeText(getApplicationContext(), "已经是第一首歌曲了", Toast.LENGTH_SHORT).show();
                } }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("选项");
        getMenuInflater().inflate(R.menu.op_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TextView textView1=(TextView) findViewById(R.id.timeStartTv);
        TextView textView2=(TextView) findViewById(R.id.timeEndTv);
        switch ( item.getItemId() ) {
            case R.id.item_delete:
                if(currIndex==-1);
                else list.remove(currIndex);
                seekBar.setProgress(0);
                textView1.setText(sdf.format(0));
                textView2.setText(sdf.format(0));
                mp.reset();
                setListui();
                //删除的代码
                return true;
            default:
                return false;
        }
    }

    //处理进度条
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            TextView songstart=(TextView)findViewById(R.id.timeStartTv);
            songstart.setText(sdf.format(progress));
            TextView songend=(TextView)findViewById(R.id.timeEndTv);
            songend.setText(sdf.format((mp.getDuration()-progress)));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mp.seekTo(seekBar.getProgress());
            isChanging=false;
        }

    }

    private void start() {
        if (list.size() > 0 && currIndex < list.size()) {
            song_path =list.get(currIndex).getPath();  //获取音乐路径
            //改变控制器文字
            TextView song = (TextView)findViewById(R.id.songName);
            TextView singer = (TextView)findViewById(R.id.singer);
            song.setText(getFileName(list.get(currIndex).getPath()));
            singer.setText(getFileName(list.get(currIndex).getPath()));

            //设置进度条
            seekBar.setMax(mp.getDuration());
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(isChanging==true) {
                        return;
                    }
                    seekBar.setProgress(mp.getCurrentPosition());

                }
            };
            timer.schedule(timerTask,0,10);


            listView.setItemChecked(currIndex, true); //设置选中 随时更新点中状态
            try {

                mp.reset(); //重置
                mp.setDataSource(song_path); //设置音乐源
                mp.prepare(); //准备
                mp.start(); //播放
            } catch (Exception e) {
            }
        } else {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileName(String pathandname){

        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");

        if(start!=-1 && end!=-1){
            return pathandname.substring(start+1,end);
        }else{

            return null;
        }
    }

    //用于判断权限是否授权成功
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if( requestCode==100 ){
            if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                //授权成功，把处理函数调用一下，如 playMusic();
            }else{
                //授权拒绝，友情提示一下
                Toast.makeText(this,"授权成功",Toast.LENGTH_SHORT).show();
            } } }

    //按返回键结束Activity时触发onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mp!=null ){
            mp.stop();
            mp.release();
        }
        Toast.makeText(getApplicationContext(), "退出啦", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Init();
        playerModel();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu); //保留
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.item1:
                readFile();

                return true;
            case R.id.item2:
                if(ModelChang==1){
                item.setTitle("单曲循环");
                }else
                item.setTitle("顺序播放");
                changModel();
                return true;
            default: return false;
        }
    }

    public void changModel(){
        MenuItem tV=(MenuItem) findViewById(R.id.item2);
        //1是顺序播放  2是单曲循环
        if(ModelChang==1){
            ModelChang=2;
        }
        else if(ModelChang==2){
            ModelChang=1;
        }

    }

    public void playerModel(){
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer sm) {
                if(ModelChang==1){
                    if(currIndex==list.size()-1){
                        currIndex=0;
                        start();
                    }
                    else if(currIndex<list.size()){
                        currIndex++;
                        start();
                    }
                }
                else if(ModelChang==2){
                    start();
                }

            }
        });

        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });

    }

    public void readFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//关键！多选参数
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,"未找到文件管理应用，请安装文件管理应用后再试",Toast.LENGTH_SHORT).show();
        }
    }

    //@SuppressLint("NewApi")  通过传递到NewApi抑制警告
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //填充用数据
        String song;
        String singer;
        long duration;
        String time;

        if (resultCode == Activity.RESULT_OK) {
            if (data.getData() != null) {
                //单次点击未使用多选的情况
                try {
                    Uri uri = data.getData();
                    //对获得的uri做解析
                    String path = getFilePathFromURI(getApplicationContext(),uri);
                    Log.d("TAG", path+"aaa");
                    /*if(!list.contains(AddList(path))) */list.add(AddList(path));
                    setListui();
                    /*else {
                        Toast.makeText(this,"歌曲已经存在",Toast.LENGTH_SHORT).show();
                    }*/
                    Log.d("TAG", "complete");

                } catch (Exception e) {
                }
            } else {
                //长按使用多选的情况
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    List<String> pathList = new ArrayList<>();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        if(uri!=null){
                            String path = getFilePathFromURI(getApplicationContext(),uri);
                            list.add(AddList(path));

                        }
                    }
                    setListui();
                }
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public MusicBean AddList(String path){
        MediaPlayer mediaPlayer=new MediaPlayer();
        String song;
        String singer;
        long duration;
        String time;
        //数据填充
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        song=getFileName(path);
        singer=getFileName(path);
        duration= mediaPlayer.getDuration();
        time=sdf.format(new Date(duration));
        Log.d("TAG", "song="+song);
        Log.d("TAG", "time="+time);
        Log.d("TAG", "time="+Long.toString(duration));
        MusicBean bean = new MusicBean(Integer.toString(list.size()+1),song,singer,path,time);
        Log.d("TAG", "runhere");
        Log.d("TAG", bean.getPath()+"ccc");

        return bean;
    }

    //获取绝对路径
    public String getFilePathFromURI(Context context, Uri contentUri) {
        File rootDataDir = context.getExternalFilesDir(null);
        Log.d("TAG",contentUri.getPath());
        String fileName = getFileNameURI(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(rootDataDir + File.separator + fileName);
            copyFile(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileNameURI(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copyFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }

}


