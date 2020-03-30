package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.List;


public class MemberInfo_Adaper extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<MemberInfo> memberInfoList;
    private Context context;

    MemberInfo_Adaper(Context context, List<MemberInfo> memberInfos){
        this.context = context;
        this.memberInfoList = memberInfos;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return memberInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return memberInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return memberInfoList.indexOf(getItem(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(view==null){
            view = layoutInflater.inflate(R.layout.member_info, null);
            viewHolder = new ViewHolder(
                    (TextView) view.findViewById(R.id.TV_name),
                    (TextView) view.findViewById(R.id.TV_id),
                    (ImageView) view.findViewById(R.id.IV),
                    (ConstraintLayout) view.findViewById(R.id.linearLayout)
            );
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        final MemberInfo memberInfo = (MemberInfo) getItem(i);
        viewHolder.txtName.setText(memberInfo.getName());
        viewHolder.txtID.setText(memberInfo.getID());
        viewHolder.imageView.setImageBitmap(memberInfo.getBmpHead());
        viewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Member_Edit.class);
                intent.putExtra("sID", memberInfo.getID());
                context.startActivity(intent);
            }
        });

        return view;
    }

    private class ViewHolder {
        TextView txtID;
        TextView txtName;
        ImageView imageView;
        ConstraintLayout constraintLayout;

        public ViewHolder(TextView txtName, TextView txtID, ImageView imageView, ConstraintLayout constraintLayout){
            this.txtName = txtName;
            this.txtID = txtID;
            this.imageView = imageView;
            this.constraintLayout = constraintLayout;
        }
    }
}
