package com.example.grocerylist;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> myList;
    final private int rowLayout;
    private Context mContext;
    int images[];


    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_my_adapter, viewGroup, false);
        return new ViewHolder(v);
    }
    // @Override
    //protected void onCreate(Bundle savedInstanceState) {
    //  super.onCreate(savedInstanceState);
    //setContentView(R.layout.activity_my_adapter);
    //}

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder viewHolder, int i) {
        final String entry = myList.get(i);
        viewHolder.myName.setText(entry);
        viewHolder.Pic.setImageResource(images[i]);
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, IdeasActivity.class);
                intent.putExtra("product", entry);
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return myList == null ? 0 : myList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView myName;
        public ImageView Pic;
        public Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            myName = (TextView) itemView.findViewById(R.id.name);
            Pic = (ImageView) itemView.findViewById(R.id.pic);
            button=(Button) itemView.findViewById(R.id.but);
        }
    }

    public MyAdapter(List<String> myList, int rowLayout, Context context, int[] images) {
        this.myList = myList;
        this.rowLayout = rowLayout;
        this.mContext = context;
        this.images = images;
    }
}