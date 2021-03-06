package com.zpj.bus.demo;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.bus.Consumer;
import com.zpj.bus.ZBus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tvText;
    private Button btnInit;
    private ViewGroup view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main, null, false);
        setContentView(view);



        tvText = findViewById(R.id.tv_text);
        btnInit = findViewById(R.id.btn_init);

        findViewById(R.id.btn_remove_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "通过Tag移除Observer" + TAG, Toast.LENGTH_SHORT).show();
                ZBus.removeObservers(TAG);
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZBus.post(TAG);
                ZBus.post(TAG, "triple", true, 1.0);
                ZBus.post(TAG, "paor", true, 10000);
                ZBus.post(TAG, "single", 5000);
            }
        });

        findViewById(R.id.btn_remove_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvText.getParent() != null ) {
                    Toast.makeText(getApplicationContext(), "移除View:" + tvText, Toast.LENGTH_SHORT).show();
                    view.removeView(tvText);
                }
            }
        });

        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });


        ZBus.with(this)
                .observe(TAG, String.class, Boolean.class, Double.class)
                .bindLifecycle(this)
                .bindLifecycle(this, Lifecycle.Event.ON_PAUSE)
                .doOnChange(new ZBus.TripleConsumer<String, Boolean, Double>() {
                    @Override
                    public void onAccept(String s, Boolean aBoolean, Double aDouble) {
                        Toast.makeText(getApplicationContext(), "接收到s=" + s + " b=" + aBoolean + " d=" + aDouble, Toast.LENGTH_SHORT).show();
                    }
                })
                .doOnDetach(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "triple doOnDetach", Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();

        ZBus.with(this)
                .observe(TAG, String.class)
                .bindLifecycle(this)
                .bindLifecycle(this, Lifecycle.Event.ON_PAUSE)
                .doOnChange(new ZBus.SingleConsumer<String>() {
                    @Override
                    public void onAccept(String s) {
                        Toast.makeText(getApplicationContext(), "接收到s=" + s, Toast.LENGTH_SHORT).show();
                    }
                })
                .doOnDetach(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "single doOnDetach", Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();

        ZBus.with(this)
                .observe(TAG, String.class, Boolean.class)
                .bindLifecycle(this)
                .bindLifecycle(this, Lifecycle.Event.ON_PAUSE)
                .doOnChange(new ZBus.PairConsumer<String, Boolean>() {
                    @Override
                    public void onAccept(String s, Boolean b) {
                        Toast.makeText(getApplicationContext(), "接收到s=" + s + " b=" + b, Toast.LENGTH_SHORT).show();
                    }
                })
                .doOnDetach(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "pair doOnDetach", Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();

        init();
    }

    private void init() {
        Toast.makeText(getApplicationContext(), "start RxObserver!", Toast.LENGTH_SHORT).show();
        btnInit.setVisibility(View.GONE);
        if (tvText.getParent() == null) {
            view.addView(tvText);
        }

        ZBus.withSticky(tvText)
                .observe(TAG)
//                .observeOn(Schedulers.io())
                .bindLifecycle(this)
                .bindTag(TAG)
                .bindLifecycle(this, Lifecycle.Event.ON_PAUSE)
                .doOnAttach(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "doOnAttach time=" + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                    }
                })
                .doOnChange(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Toast.makeText(getApplicationContext(), "收到信息 time=" + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                        if (tvText.getVisibility() == View.VISIBLE) {
                            tvText.setVisibility(View.GONE);
                        } else {
                            tvText.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .doOnDetach(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "doOnDetach time=" + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                        btnInit.setVisibility(View.VISIBLE);
                    }
                })
                .subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(getApplicationContext(), "onPause", Toast.LENGTH_SHORT).show();
    }

}
