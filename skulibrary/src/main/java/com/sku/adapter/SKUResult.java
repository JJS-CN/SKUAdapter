package com.sku.adapter;

import java.util.List;

/**
 * 说明：
 * Created by jjs on 2018/11/19.
 */

public class SKUResult {
    public SKUdata mSKUdata;
    public List<String> mResults;

    public SKUResult(SKUdata SKUdata, List<String> results) {
        mSKUdata = SKUdata;
        mResults = results;
    }
}
