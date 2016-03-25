package com.sampson.android.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sampson.android.demo.config.Config;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.ll_image_container)
    LinearLayout llImageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initHorizontalView();
    }

    private void initHorizontalView() {
        int j = 0;
        for (int i = 0; i < 1000; i++) {
            View imgContainer = LayoutInflater.from(this).inflate(R.layout.item_gallery, null);
            ImageView img = (ImageView) imgContainer.findViewById(R.id.item_img);
            if (j == Config.imgs.length) {
                j = 0;
            }
            Picasso.with(this).load(Config.imgs[j]).resize(200, 200).centerCrop().into(img);
            llImageContainer.addView(imgContainer);
            j++;
        }

    }
}
