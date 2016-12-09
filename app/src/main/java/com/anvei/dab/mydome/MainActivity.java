package com.anvei.dab.mydome;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.anvei.dab.mydome.net.NetClass;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void click(View view) {
        NetClass.getInstance().downloadFile(Constant.baseUrl+Constant.fileName);
        Toast.makeText(this, "dsada", Toast.LENGTH_SHORT).show();
    }

    public void click1(View view) {
        NetClass.getInstance().mDisposable.dispose();
    }
}
