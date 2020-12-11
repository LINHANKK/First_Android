package com.example.first.adapter;

import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first.base.AppInfo;
import com.example.first.R;

import java.util.ArrayList;
import java.util.List;


public class RvAdapter extends RecyclerView.Adapter<RvAdapter.inHolder> {

    List<AppInfo> resolveInfo = new ArrayList<>();
    PackageManager packageManager;
    RvAdapter.OnItemClickListener onItemClickListener;

    public void setResolveInfo(List<AppInfo> resolveInfo) {
        this.resolveInfo.clear();
        this.resolveInfo.addAll(resolveInfo);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public RvAdapter(List<AppInfo> resolveInfo, PackageManager packageManager) {
        this.resolveInfo = resolveInfo;
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public inHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inHolder inHolder = new inHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app,parent,false));
        return inHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull inHolder holder, int position) {
        AppInfo info = resolveInfo.get(position);
        holder.imageView.setImageDrawable(info.getAppIcon());
        holder.textView.setText(info.getAppName());

        //点击打开app
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });

        //长按app卸载
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemClickListener != null) {
                    int position1 = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, position1);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return resolveInfo.size();
    }

    public class inHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public inHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv);
            textView = itemView.findViewById(R.id.tv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
}
