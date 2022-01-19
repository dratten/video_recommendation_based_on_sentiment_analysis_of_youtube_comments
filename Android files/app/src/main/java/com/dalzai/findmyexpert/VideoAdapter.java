package com.dalzai.findmyexpert;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.Viewholder>
{
    private Context context;
    private ArrayList<VideoModel> videoModelArrayList;

    public VideoAdapter(Context context, ArrayList<VideoModel> videoModelArrayList) {
        this.context = context;
        this.videoModelArrayList = videoModelArrayList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_card, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        final VideoModel model = videoModelArrayList.get(position);
        Glide.with(context)
                .load(model.getVideo_thumbnail())
                .into(holder.videoThumbnail);
        holder.videoTitle.setText(model.getVideo_title());
        holder.videoChannel.setText(model.getChannel_title());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent video = new Intent(v.getContext(), VideoActivity.class);
                video.putExtra("VideoID", model.getVideo_id());
                video.putExtra("VideoTitle", model.getVideo_title());
                video.putExtra("VideoChannel", model.getChannel_title());
                context.startActivity(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoModelArrayList.size();
    }


    public static class Viewholder extends RecyclerView.ViewHolder
    {
        ImageView videoThumbnail;
        TextView videoTitle;
        RelativeLayout relativeLayout;
        TextView videoChannel;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.video_card_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_card_title);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
            videoChannel = itemView.findViewById(R.id.video_card_channel);
        }
    }
}
