package com.example.grocerylist;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class IdeasActivity extends AppCompatActivity {

    public static String username;
    RecyclerView mRecyclerView;
    MyAdapter mAdapter;
    int image_id[] ={R.drawable.fruitsandveggies, R.drawable.dairy, R.drawable.meat, R.drawable.pasta,R.drawable.hygiene, R.drawable.cleaning,R.drawable.alcohol };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ideas);


        List<String> values = Arrays.asList("Fruits&Veggies", "DairyProducts", "MeatProducts", "Pasta","HygieneProducts", "CleaningProducts", "Alcohol" );
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MyAdapter(values, R.layout.activity_my_adapter, this, image_id);
        mRecyclerView.setAdapter(mAdapter);
    }
    public static String getUsername() {
        return username;
    }

}