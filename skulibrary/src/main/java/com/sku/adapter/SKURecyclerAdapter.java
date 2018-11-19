package com.sku.adapter;

import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 说明：SKU选择，需要保证每个选项按顺序排列，不可乱序
 * Created by jjs on 2018/11/15.
 */

public abstract class SKURecyclerAdapter extends BaseSectionQuickAdapter<SKUSelectEntity, BaseViewHolder> {
    private String[] mSelectArr;//按钮选中列表
    private SKUListener mSKUListener;//选择监听，选中时会
    private int mTitleSize;//选择项的类型总数
    private List<String> mTitleTableList;//每个项的table标题---次要
    private List<SKUdata> mSKUdataList;//服务器发回的可选线路---主要
    private List<SKUSelectEntity> mAdapterData;//用于UI展示的adapter

    public SKURecyclerAdapter(int layoutResId, int sectionHeadResId) {
        super(layoutResId, sectionHeadResId, null);
    }

    /**
     * 设置监听，通过isSelectAll判断是否全部选择完全；再通过data强转为对应实体类获取对应的选择数据
     *
     * @param skuListener
     */
    public void setSKUListener(SKUListener skuListener) {
        mSKUListener = skuListener;
    }

    /**
     * 设置SKU参数
     * 优先从getSelctValues中取值，其次将从getSKUdatas中遍历所有可能选项
     *
     * @param skuData 传入数据实体类--以此来构造用于UI展示的实际列表
     */
    public void setSKUdata(SKUEntity skuData) {
        //取值保存，这样不会造成多次取值
        mTitleTableList = skuData.getSKUtitles() != null ? skuData.getSKUtitles() : new ArrayList<String>();
        mSKUdataList = skuData.getSKUdatas() != null ? skuData.getSKUdatas() : new ArrayList<SKUdata>();
        List<List<String>> selectValueList = skuData.getSelctValues() != null ? skuData.getSelctValues() : new ArrayList<List<String>>();

        if (mSKUdataList.size() > 0 && mSKUdataList.get(0).getSKUdatas().size() > 0) {
            //如果有服务器数据，从数据中获取选项数
            mTitleSize = mSKUdataList.get(0).getSKUdatas().size();
        } else if (selectValueList.size() > 0) {
            mTitleSize = selectValueList.size();
        } else if (mTitleTableList.size() > 0) {
            mTitleSize = mTitleTableList.size();
        }

        //初始化选择数组
        mSelectArr = new String[mTitleSize];
        //初始化-用于adapter使用的entitys
        mAdapterData = new ArrayList<>();

        if (selectValueList.size() > 0) {
            /** 优先从用户提供的参数列表中取值 (能够出现一直不可选的空值数据)*/
            for (int i = 0; i < selectValueList.size(); i++) {
                //添加header
                mAdapterData.add(new SKUSelectEntity(true, mTitleTableList.size() > i ? mTitleTableList.get(i) : ""));
                List<String> valist = selectValueList.get(i);
                for (int j = 0; j < valist.size(); j++) {
                    //添加item
                    mAdapterData.add(new SKUSelectEntity(i, valist.get(j)));
                }
            }
        } else {
            /**从商品分类表中遍历参数 （只会出现后台提供了商品内容的参数）（初始化复杂度提高）*/
            List<List<SKUSelectEntity>> datas = new ArrayList<>();
            for (int i = 0; i < mTitleTableList.size(); i++) {
                //由于商品列表数据接口为123123，而按钮需要的为112233进行添加
                List<SKUSelectEntity> step = new ArrayList<>();
                for (int j = 0; j < mSKUdataList.get(i).getSKUdatas().size(); j++) {
                    step.add(new SKUSelectEntity(j, mSKUdataList.get(i).getSKUdatas().get(j)));
                }
                datas.add(step);
            }
            for (int i = 0; i < mTitleTableList.size(); i++) {
                mAdapterData.add(new SKUSelectEntity(true, mTitleTableList.size() > i ? mTitleTableList.get(i) : ""));
                for (int j = 0; j < datas.size(); j++) {
                    //遍历服务器数据，可能其中一些值选项是相同的，给用户选择时需要清除重复
                    boolean isCon = false;
                    for (int k = 0; k < mAdapterData.size(); k++) {
                        if (mAdapterData.get(k).t != null && mAdapterData.get(k).t.equals(datas.get(j).get(i).t)) {
                            isCon = true;
                            break;
                        }
                    }
                    if (!isCon) {
                        //注意add取j再i，取不同list的相同位置
                        mAdapterData.add(datas.get(j).get(i));
                    }
                }
            }
        }
        super.setNewData(mAdapterData);
        //todo 设置完数据先执行一次，更新values导致的初始不可选状态
        reCommSpec();
    }


    @Override
    protected void convertHead(BaseViewHolder helper, SKUSelectEntity item) {
        convertHeader(helper, item);
    }

    //绑定header数据，参数取t或者header
    public abstract void convertHeader(BaseViewHolder helper, SKUSelectEntity item);

    //绑定item数据，参数取t
    public abstract void convertItem(BaseViewHolder helper, SKUSelectEntity item);

    @Override
    protected void convert(BaseViewHolder helper, final SKUSelectEntity item) {
        helper.itemView
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //如果status为1，不可选
                        if (item.status == 1) {
                            return;
                        }
                        String value = mSelectArr[item.mType];
                        if (value != null && value.equals(item.t)) {
                            //如果选择了已选项，置为空
                            mSelectArr[item.mType] = null;
                        } else {
                            //否则添加对应选项
                            mSelectArr[item.mType] = item.t;
                        }
                        //判断是否全部勾选---用于执行选中回调
                        boolean isSelectAll = true;
                        for (String aMSelectArr : mSelectArr) {
                            if (aMSelectArr == null) {
                                isSelectAll = false;
                            }
                        }
                        if (mSKUListener != null) {
                            if (isSelectAll) {
                                for (int i1 = 0; i1 < mSKUdataList.size(); i1++) {
                                    List<String> mmm = mSKUdataList.get(i1).getSKUdatas();
                                    boolean canClick = true;
                                    //判断子集
                                    for (int k2 = 0; k2 < mmm.size(); k2++) {
                                        if (mSelectArr[k2] != null && !mSelectArr[k2].equals(mmm.get(k2))) {
                                            canClick = false;
                                            break;
                                        }
                                    }
                                    if (canClick) {
                                        //全选中，返回对应数据
                                        mSKUListener.onSelect(true, mSKUdataList.get(i1));
                                        break;
                                    }
                                }
                            } else {
                                //未选中，返回数据为null
                                mSKUListener.onSelect(false, null);
                            }
                        }

                        //选中时，需要对应顺序进行组合判断
                        reCommSpec();
                        notifyDataSetChanged();
                    }
                });
        convertItem(helper, item);
    }


    //重判是否可选  //得到所有的可能性集合---判断可能性集合是否实际可用
    private void reCommSpec() {
        for (int i = 0; i < mAdapterData.size(); i++) {
            //adapter按钮数据
            SKUSelectEntity entity = mAdapterData.get(i);
            //不是标题
            if (entity.isHeader) {
                continue;
            }
            //新建一个与选中数组size相同的可能选中数组--存放可能选中集合
            String[] mSnapSelectArr = new String[mSelectArr.length];
            //todo 生成各种用户可能选中情况---实际操作为依次替换选中数组的其中一个值
            for (int k = 0; k < mTitleTableList.size(); k++) {
                //如果当前按钮的type值与当前正在遍历的项一致
                if (entity.mType == k) {
                    //即我正在遍历这个项下的所有按钮可能性，这时候需要取按钮的值来生成可能值
                    mSnapSelectArr[k] = entity.t;
                } else {
                    //其他位置不变
                    mSnapSelectArr[k] = mSelectArr[k];
                }
            }
            Log.i("得到下一次用户的可能选中：", Arrays.toString(mSnapSelectArr));
            entity.status = 1;//初始化为不可选
            for (int i1 = 0; i1 < mSKUdataList.size(); i1++) {
                //服务器可选择内容
                List<String> mSpecList = mSKUdataList.get(i1).getSKUdatas();
                //可能选中项---需要判断。可能选中项是否在服务器可选列表内
                boolean canClick = true;
                //判断子集
                for (int k2 = 0; k2 < mSpecList.size(); k2++) {
                    if (mSnapSelectArr[k2] != null && !mSnapSelectArr[k2].equals(mSpecList.get(k2))) {
                        canClick = false;
                        break;
                    }
                }
                if (canClick) {
                    entity.status = 0;//设置为可选
                    String vals = mSelectArr[entity.mType];
                    if (vals != null && vals.equals(entity.t)) {
                        entity.status = 2;//设置为被选中
                    }
                    break;
                }
            }
        }
    }

    public interface SKUListener {
        /**
         * 选择监听
         * @param isSelectAll 是否选择完
         * @param udata 选择数据，需isSelectAll为true才不为空
         */
        void onSelect(boolean isSelectAll, SKUdata udata);
    }


}
