package com.sku.adapter;

import java.util.List;

/**
 * 说明：
 * Created by jjs on 2018/11/15.
 */

public interface SKUEntity {
    //获取分类标题
    List<String> getSKUtitles();

    //获取商品的列表
    List<SKUdata> getSKUdatas();

    //获取SKU 选项列表
    List<List<String>> getSelctValues();


}
