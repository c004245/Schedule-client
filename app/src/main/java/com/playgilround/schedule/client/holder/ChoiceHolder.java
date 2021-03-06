package com.playgilround.schedule.client.holder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.playgilround.schedule.client.R;

/**
 * 18-10-20
 * 스케줄 저장 시 친구 선택 ViewHolder
 */
public class ChoiceHolder extends RecyclerView.ViewHolder {

    public ImageView userImage;
    public TextView userNickName;
    public CheckBox checkBox;

    public ChoiceHolder(View itemView) {
        super(itemView);

        checkBox = itemView.findViewById(R.id.checkbox);
        userImage = itemView.findViewById(R.id.userImage);
        userNickName = itemView.findViewById(R.id.userNickName);
    }
}
