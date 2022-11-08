package com.dam.musicplayer;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

// Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView
public class AdapterSong extends RecyclerView.Adapter<AdapterSong.ViewHolder> {

    Context context;
    ArrayList<ModelSong> songArrayList;

    // A ViewHolder describes an item view and metadata about its place within the RecyclerView.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView cover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            artist = itemView.findViewById(R.id.tvArtist);
            cover = itemView.findViewById(R.id.ivCover);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /* VERSION AVEC INTERFACE PERSO  */
                    // getAdapterPosition from RecyclerView.ViewHolder
                    onItemClickListener.onItemClick(getAdapterPosition(), view);

                    /* VERSION AVEC INTERFACE STD */
                    // onItemClickListener.onItemClick(AdapterSong.this, view, AdapterSong.getAdapterPosition() );
                }
            });
        }
    }

    /* VERSION AVEC INTERFACE PERSO DEBUT */
    // nested interface declaration
    public interface OnItemClickListener {
        void onItemClick(int pos, View v);
    }

    // an interface object
    public OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    /* VERSION AVEC INTERFACE PERSO FIN */
    /* ---------------------------------------------------------------------- */
    /* VERSION AVEC INTERFACE STD */
//    private AdapterView.OnItemClickListener onItemClickListener; // TODO

/*
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
*/

    /* ---------------------------------------------------------------------- */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(songArrayList.get(position).getSongTitle());
        holder.artist.setText(songArrayList.get(position).getSongArtist());
        Uri imgUri = songArrayList.get(position).getSongCover();
        /* Glide */
        RequestOptions options = new RequestOptions()
                .centerCrop() // center / crop pour les images de remplacement
                .error(R.drawable.ic_music_note_white_24) // cas erreur
                .placeholder(R.drawable.ic_music_note_white_24); // cas pas d'image
        Context context = holder.cover.getContext();
        Glide.with(context)
                .load(imgUri)
                .apply(options)
                .fitCenter()
                .override(150, 150)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.cover);
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public AdapterSong(Context context, ArrayList<ModelSong> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
    }


}
