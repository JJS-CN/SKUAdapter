package com.adapter.sku;

import com.sku.adapter.SKUEntity;
import com.sku.adapter.SKUdata;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明：
 * Created by jjs on 2018/11/19.
 */

public class TestDemo {

    public TestDemo() {
        this.data = new DataEntity();
        List<DataEntity.SpecListEntity> ml = new ArrayList<>();
        ml.add(new DataEntity.SpecListEntity("白", "大", "马克A型"));
        ml.add(new DataEntity.SpecListEntity("白", "小", "马克B型"));
        ml.add(new DataEntity.SpecListEntity("黑", "中", "马克B型"));
        data.specList = ml;
    }


    public DataEntity data;

    public static class DataEntity implements SKUEntity {

        public List<SpecListEntity> specList;

        @Override
        public List<String> getSKUtitles() {
            //返回每项选项的标题。会获取size以进行可选项的初始化操作，涉及到后续逻辑判断。
            //个数需要
            List<String> mlist
                    = new ArrayList<>();
            mlist.add("颜色");
            mlist.add("大小");
            mlist.add("型号");
            return mlist;
        }

        @Override
        public List<SKUdata> getSKUdatas() {
            //返回所有SKU条目，
            List<SKUdata> list = new ArrayList<>();
            list.addAll(specList);
            return list;
        }

        @Override
        public List<List<String>> getSelctValues() {
            //手动设置按钮可选项，能自由设定服务器未出现的字段。
            //否则将从skuData中，遍历所有SKU条目，查找出所有可选内容
            List<List<String>> list = new ArrayList<>();
            List<String> str1 = new ArrayList<>();
            str1.add("黑");
            str1.add("白");
            str1.add("红");
            List<String> str3 = new ArrayList<>();
            str3.add("大");
            str3.add("中");
            str3.add("小");
            List<String> str2 = new ArrayList<>();
            str2.add("马克A型");
            str2.add("马克B型");
            str2.add("马克C型");
            list.add(str1);
            list.add(str3);
            list.add(str2);
            return list;
        }

        public static class SpecListEntity implements SKUdata {
            public String spec1;
            public String spec2;
            public String spec3;

            public SpecListEntity(String spec1, String spec2, String spec3) {
                this.spec1 = spec1;
                this.spec2 = spec2;
                this.spec3 = spec3;
            }

            @Override
            public List<String> getSKUdatas() {
                //这里是服务器可选项组合--具体的SKU商品型号
                //通过数组返回每个SKU组合中，各个选项参数
                List<String> list = new ArrayList<>();
                list.add(spec1);
                list.add(spec2);
                list.add(spec3);
                return list;
            }
        }
    }
}
