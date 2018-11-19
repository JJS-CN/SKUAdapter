package com.sku.adapter;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * 说明：
 * Created by jjs on 2018/11/15.
 */

public class SKUSelectEntity extends SectionEntity<String> {
    public int mType;
    public int status;//0可选，1不可选，2被选中

    public SKUSelectEntity(boolean isHeader, String header) {
        super(isHeader, header);
        t = header;
    }

    public SKUSelectEntity(int type, String name) {
        super(name);
        mType = type;
    }
}
