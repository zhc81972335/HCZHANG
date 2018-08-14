package com.whzydz.mycustomview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    HCRecyclerView rv;

    ArrayList<String> data = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i = 0; i < 20; i ++) {
            data.add("data" + i);
        }

        rv = (HCRecyclerView)findViewById(R.id.rv);
        rv.setLayoutManager(new HCRecyclerView.HCLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        List<Map<Integer, Object>> data = new ArrayList<Map<Integer, Object>>();
        for(int i = 0; i < 20; i ++) {
            Map<Integer, Object> item = new HashMap<Integer, Object>();
            item.put(R.id.tv_name, "zhang" + i);
            item.put(R.id.tv_age, 5 + i);
            data.add(item);
        }

        HCRecyclerView.HCAdapter adapter = new HCRecyclerView.HCAdapter(this, data, R.layout.item, R.mipmap.ic_launcher);
        rv.setAdapter(adapter);

        rv.setItemClickListener(new HCRecyclerView.ItemClickListener() {
            @Override
            public void onDelete(int pos) {
                Log.i("hczhang", "delete " + pos);
            }
        });
    }

}
