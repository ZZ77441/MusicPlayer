package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.util.List;

public class MusicAdapter extends ArrayAdapter<MusicBean> {
    private int resourceId;
    public MusicAdapter(@NonNull Context context, int resource, @NonNull List<MusicBean> objects) {
        super(context, resource, objects);
        resourceId=resource; //添加语句
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicBean musicBean = getItem(position); //获取当前选中的music实例
        //获取行布局及其各个元素
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView songid =(TextView) view.findViewById(R.id.CardNum) ;
        TextView songName =(TextView) view.findViewById(R.id.CardViewSong);
        TextView singer =(TextView) view.findViewById(R.id.CardViewSinger);
        TextView duration = (TextView) view.findViewById(R.id.CardDuration);
        //填充数据
        songid.setText(musicBean.getId());
        songName.setText(musicBean.getSong());
        singer.setText(musicBean.getSinger());
        duration.setText(musicBean.getDuration());
        return view;
    }

}
