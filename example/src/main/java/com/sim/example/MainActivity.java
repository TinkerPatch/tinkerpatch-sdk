package com.sim.example;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.sim.example.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainViewModel = new MainViewModel(this);
        binding.setViewModel(mainViewModel);
        ((TextView) findViewById(R.id.response_tv)).setMovementMethod(new ScrollingMovementMethod());
    }
}
