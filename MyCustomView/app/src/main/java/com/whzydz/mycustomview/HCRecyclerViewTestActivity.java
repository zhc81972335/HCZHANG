package com.whzydz.mycustomview;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HCRecyclerViewTestActivity extends AppCompatActivity {
    private HCRecyclerView rv;

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_recyclerview);

        List<Data> data = new ArrayList<Data>();
        for(int i = 0; i < 20; i ++) {
            data.add(new Data("zhang" + i, "" + i));
        }

        rv = (HCRecyclerView)findViewById(R.id.rv);
        rv.setLayoutManager(new HCRecyclerView.HCLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new MyAdapter(this, data, R.layout.item);
        rv.setAdapter(adapter);
        rv.setDeleteListener(new HCRecyclerView.DeleteListener() {
            @Override
            public void onDelete(int pos) {
                adapter.removeItem(pos);
            }
        });
    }

    class MyAdapter extends HCRecyclerView.HCAdapter<Data, MyViewHolder> {

        /**
         * adapter构造器
         *
         * @param context      ：上下文
         * @param data         ：数据集合，每一个元素，代表了控件id与数据的对应关系
         * @param itemLayoutId ：每一行的view
         */
        public MyAdapter(Context context, List<Data> data, int itemLayoutId) {
            super(context, data, itemLayoutId);
        }

        @Override
        public MyViewHolder createMyViewHolder(View v) {
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Data d = data.get(position);
            MyViewHolder myHolder = (MyViewHolder)holder;
            myHolder.getTvName().setText(d.getName());
            myHolder.getTvAge().setText(d.getAge());
        }
    }

    class MyViewHolder extends HCRecyclerView.HCViewHolder {
        TextView tvName;
        TextView tvAge;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView)itemView.findViewById(R.id.tv_name);
            tvAge = (TextView)itemView.findViewById(R.id.tv_age);
        }

        public TextView getTvName() {
            return tvName;
        }

        public TextView getTvAge() {
            return tvAge;
        }
    }

    class Data {
        private String name;
        private String age;

        public Data(String name, String age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }
    }

}
