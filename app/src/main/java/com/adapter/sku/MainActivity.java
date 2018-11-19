package com.adapter.sku;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.chad.library.adapter.base.BaseViewHolder;
import com.sku.adapter.SKURecyclerAdapter;
import com.sku.adapter.SKUSelectEntity;
import com.sku.adapter.SKUdata;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mRv = findViewById(R.id.rv);
        //设置布局方式
        mRv.setLayoutManager(new GridLayoutManager(this, 3));
        //传入item布局 和 header布局
        SKURecyclerAdapter adapter = new SKURecyclerAdapter(R.layout.item_sku_data, R.layout.item_sku_header) {
            @Override
            public void convertHeader(BaseViewHolder helper, SKUSelectEntity item) {
                helper.setText(R.id.tv_name, item.t);
            }

            @Override
            public void convertItem(BaseViewHolder helper, SKUSelectEntity item) {
                //根据item.status来展示不同UI样式，无需做点击判断。
                //todo 不能设置itemView的点击事件，因为会覆盖掉点击处理逻辑，实在需要请把相关代码复制出来
                helper.setText(R.id.tv_name, item.t)
                        .setBackgroundColor(R.id.tv_name, item.status == 2 ? Color.RED : item.status == 1 ? Color.GRAY:Color.BLACK );
            }
        };
        adapter.setSKUListener(new SKURecyclerAdapter.SKUListener() {
            @Override
            public void onSelect(boolean isSelectAll, SKUdata udata) {
                //选择监听，
                if (isSelectAll) {
                    TestDemo.DataEntity.SpecListEntity mSpecEntity = (TestDemo.DataEntity.SpecListEntity) udata;
                    Log.e("eeee", mSpecEntity.spec1 + " + " + mSpecEntity.spec2 + " + " + mSpecEntity.spec3);
                }
            }
        });
        mRv.setAdapter(adapter);
        TestDemo demo=new TestDemo();
        adapter.setSKUdata(demo.data);
    }
}
