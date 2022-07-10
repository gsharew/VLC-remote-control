package com.example.VlcStream.FetchComputerFiles;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VlcStream.BasicControl.Playlist;
import com.example.VlcStream.ComputerControl.Control;
import com.example.VlcStream.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Executors;

public class ComputerFileAdapter extends RecyclerView.Adapter<ComputerFileAdapter.ViewHolder> {
    ArrayList<String> fileCollection;
    Context context;
    static Stack<String> historyPathContainer = new Stack<>();
    ArrayList<String> filetype = new ArrayList<>();
    ComputerFileAdapter recyclerAdapter;
    public ComputerFileAdapter(Context context, ArrayList<String> fileCollection) {
        this.context = context;
        this.fileCollection = fileCollection;
        this.recyclerAdapter = this;
        filetype.add("ps");
        filetype.add("wmv");
        filetype.add("ogg");
        filetype.add("dv");
        filetype.add("TS");
        filetype.add("wma");
        filetype.add("ogm");
        filetype.add("mxf");
        filetype.add("pva");
        filetype.add("mp4");
        filetype.add("wav");
        filetype.add("nut");
        filetype.add("ES");
        filetype.add("asf");
        filetype.add("mkv");
        filetype.add("m4a");
        filetype.add("mp3");
        filetype.add("mov");
        filetype.add("webm");
        filetype.add("mts");
        filetype.add("mpeg-4");
        filetype.add("aac");
        filetype.add("alac");
        filetype.add("flac");
        filetype.add("avi");
        filetype.add("3gp");
        filetype.add("flv");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =  LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.computers_file_view_model, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            String fileTypeToBeRendered = FetchComputerData.computerFileContainer.get(position).getType();
            holder.textView.setText(fileCollection.get(position));

            if (fileTypeToBeRendered.equalsIgnoreCase("dir")) {
                holder.imageView.setImageResource(R.drawable.folder);
            } else {
                holder.imageView.setImageResource(R.drawable.singlefile);
            }

            holder.parentLayout.setOnClickListener(view -> {
                if (FetchComputerData.computerFileContainer.size() !=0) {
                    String fileType2 = FetchComputerData.computerFileContainer.get(holder.getAbsoluteAdapterPosition()).getType();

                    //this will fetch if the file type is directory
                    if (fileType2.equalsIgnoreCase("dir")) {

                        //get the absolute path of the clicked item
                        String uri = FetchComputerData.computerFileContainer.get(holder.getAbsoluteAdapterPosition()).getUri();
                        historyPathContainer.push(uri);
                        new FetchDetailedComputerFile().fetchTheComputerFiles(uri, context, false, recyclerAdapter);

                    } else {
                        String clickedItem = FetchComputerData.computerFileContainer.get(holder.getAbsoluteAdapterPosition()).getName();
                        String clickedItemUri = FetchComputerData.computerFileContainer.get(holder.getAbsoluteAdapterPosition()).getUri();
                        String theActualFileExtenstion = clickedItem.substring(clickedItem.lastIndexOf(".") + 1);
                        theActualFileExtenstion = theActualFileExtenstion.toLowerCase();
                        if (filetype.contains(theActualFileExtenstion)) {
                            HandleClickedItem.notifyVLC(context, clickedItemUri);
                            Playlist.doesPreviouslyFetchd = false;
                        }
                        else
                        {
                            if(Control.socket != null)
                            {
                                Executors.newSingleThreadExecutor().execute(() ->
                                {
                                    try {
                                        DataOutputStream dataOutputStream = new DataOutputStream(Control.socket.getOutputStream());
                                        String actualMessage = "OpeningFile:" + clickedItemUri.substring(0,clickedItemUri.lastIndexOf("/") + 1) + clickedItem;
                                        dataOutputStream.writeUTF(actualMessage);
                                        Log.e("file to open", "message" + actualMessage);
                                    } catch (IOException e) {
                                        Log.e("Error happened", "message");
                                        e.printStackTrace();
                                    }
                                });
                            }
                            else
                            {
                                Log.e("Error happened", "message");
                            }
                        }
                    }
                }
            });
        }catch (Exception e)
        {
            Log.e("Error happened", "message");
        }
    }

    public static void handleChange(RecyclerView recyclerView, ComputerFileAdapter recyclerAdapter)
    {
        recyclerView.post(recyclerAdapter::notifyDataSetChanged);
    }

    @Override
    public int getItemCount() {
        return fileCollection.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        LinearLayout parentLayout;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name);
            parentLayout = itemView.findViewById(R.id.computers_file_layout);
            imageView = itemView.findViewById(R.id.file_icon);
        }
    }
}
