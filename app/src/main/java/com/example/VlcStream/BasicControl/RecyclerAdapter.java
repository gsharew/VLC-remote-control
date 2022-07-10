package com.example.VlcStream.BasicControl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.VlcStream.R;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    ArrayList<String> musicCollection;
    Context context;
    int row_index = -1;
    RecyclerAdapter recyclerAdapter;
    static RecyclerView recyclerView;
    RecyclerView recyclerViewcopy;

    public RecyclerAdapter(Context context, ArrayList<String> musicCollection, RecyclerView recyclerView) {
        this.context = context;
        this.musicCollection = musicCollection;
        this.recyclerAdapter = this;
        this.recyclerViewcopy = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        recyclerView = recyclerViewcopy;
        LayoutInflater layoutInflater =  LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.playlist_view_model, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        holder.textView.setText(musicCollection.get(position));
        holder.parentLayout.setOnClickListener(view -> {
            row_index = holder.getAbsoluteAdapterPosition();
            if (Playlist.playlistInformation.size() != 0)
            {
                String command = "pl_play&id=" + Playlist.playlistInformation.get(holder.getAbsoluteAdapterPosition()).getId();
                HandleBasicRequest.notifyVLC(context.getApplicationContext(), command);
            }
        });
    }

    public static void notifyChange()
    {
        if(recyclerView != null)
        {
            RecyclerAdapter recyclerAdapter = (RecyclerAdapter) recyclerView.getAdapter();
            assert recyclerAdapter != null;
            recyclerView.post(recyclerAdapter::notifyDataSetChanged);
        }
    }

    @Override
    public int getItemCount() {
        return musicCollection.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        LinearLayout parentLayout;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.Music_Name);
            parentLayout = itemView.findViewById(R.id.playlist_model);
            imageView = itemView.findViewById(R.id.music_icon);
        }
    }
}
