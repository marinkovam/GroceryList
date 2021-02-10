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


    RecyclerView mRecyclerView;
    MyAdapter mAdapter;
    char text_id[]={};
    int image_id[] ={R.drawable.fruitsandveggies, R.drawable.dairy, R.drawable.meat, R.drawable.pasta,R.drawable.hygiene, R.drawable.cleaning,R.drawable.alcohol };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ideas);


        List<String> values = Arrays.asList("•Tomatoes\n•Cucumber\n•Cabbage\n•Kiwi\n•Banana\n•Blueberry\n•Lemon\n•Chilli Pepper\n•Cherry\n•Eggplant",
                "•Butter\n•Cheese\n•Yoghurt\n•Milk\n•Sour Cream\n•Kefir\n•Feta\n•Cottage Cheese\n•Condensed Milk",
                "•Chicken Steak\n•Sausage\n•Pork\n•Bacon\n•Meatballs\n•Chicken Breasts\n•Ribs\n•Tuna\n•Salami",
                "•Spaghetti\n•Lasagna\n•Fusilli\n•Gnocchi\n•Riciolli\n•Ravioli\n•Orzo\n•Penne\n•Ditalini",
                "•Shampoo\n•Conditioner\n•Toothpaste\n•Breath strips\n•Dental floss\n•Mouth wash\n•Hand Soap\n•Cotton Buds\n•Razor",
                "•Sponge\n•Detergent\n•Thick Bleach\n•Laundry Soap\n•Gloves\n•Shower Cleaner\n•Soft Scrub\n•Wet Wipes\n•Domestos",
                "•Wine\n•Vodka\n•Beer\n•Whiskey\n•Gin\n•Rum\n•Sangria\n•Tequila\n•Brandy\n•Negroni" );
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MyAdapter(values, R.layout.activity_my_adapter, this, image_id);
        mRecyclerView.setAdapter(mAdapter);
    }

}