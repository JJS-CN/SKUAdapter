package com.sku.adapter;

import android.view.View;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private List<List<String>> mSelectValueList;//每个项中的选项---可空
    private List<SKUSelectEntity> mAdapterData;//用于UI展示的adapter
    private List<HashMap<String, SKUResult>> mSKUResults;//所有返回值

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
        mSelectValueList = skuData.getSelctValues() != null ? skuData.getSelctValues() : new ArrayList<List<String>>();

        if (mSKUdataList.size() > 0 && mSKUdataList.get(0).getSKUdatas().size() > 0) {
            //如果有服务器数据，从数据中获取选项数
            mTitleSize = mSKUdataList.get(0).getSKUdatas().size();
        } else if (mSelectValueList.size() > 0) {
            mTitleSize = mSelectValueList.size();
        } else if (mTitleTableList.size() > 0) {
            mTitleSize = mTitleTableList.size();
        }

        //初始化选择数组
        mSelectArr = new String[mTitleSize];
        //初始化-用于adapter使用的entitys
        mAdapterData = new ArrayList<>();

        if (mSelectValueList.size() > 0) {
            /** 优先从用户提供的参数列表中取值 (能够出现一直不可选的空值数据)*/
            for (int i = 0; i < mSelectValueList.size(); i++) {
                //添加header
                mAdapterData.add(new SKUSelectEntity(true, mTitleTableList.size() > i ? mTitleTableList.get(i) : ""));
                List<String> valist = mSelectValueList.get(i);
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
        mSKUResults = new ArrayList<>();
        powerSet(mSKUdataList);

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
                        //选中时，需要对应顺序进行组合判断
                        reCommSpec();
                        notifyDataSetChanged();
                    }
                });
        convertItem(helper, item);
    }


    //重判是否可选  //得到所有的可能性集合---判断可能性集合是否实际可用
    private void reCommSpec() {
        if (mSelectArr[0] == null) {
            //如果未选择，返回第一条数据
            setListenerReturn(false, mSKUdataList.get(0));
        }
        for (int i = 0; i < mAdapterData.size(); i++) {
            //adapter按钮数据
            SKUSelectEntity entity = mAdapterData.get(i);
            //不是标题
            if (entity.isHeader) {
                continue;
            }
            //新建一个与选中数组size相同的可能选中数组--存放可能选中集合
            List<String> mSnapList = new ArrayList<>();
            //todo 生成各种用户可能选中情况---实际操作为依次替换选中数组的其中一个值
            for (int k = 0; k < mSelectArr.length; k++) {
                //如果当前按钮的type值与当前正在遍历的项一致
                if (entity.mType == k) {
                    //即我正在遍历这个项下的所有按钮可能性，这时候需要取按钮的值来生成可能值
                    mSnapList.add(entity.t);
                } else {
                    //其他位置不变
                    if (mSelectArr[k] != null)
                        mSnapList.add(mSelectArr[k]);
                }
            }
            //    Log.i("得到下一次用户的可能选中：", mSnapList.toString());
            entity.status = 1;//初始化为不可选
            SKUResult mResult = mSKUResults.get(mSnapList.size()).get(mSnapList.toString());
            if (mResult != null) {
                entity.status = 0;//设置为可选
                String vals = mSelectArr[entity.mType];
                if (vals != null && vals.equals(entity.t)) {
                    entity.status = 2;//设置为被选中
                    if (entity.mType == 0) {
                        //如果被选中项下标为0，返回数据
                        setListenerReturn(mSnapList.size() == mTitleSize, mResult.mSKUdata);
                    }
                }
            }
        }
    }

    private SKUdata lastResult;
    private boolean lastSelectAll;

    //设置返回值
    private void setListenerReturn(boolean isSelectAll, SKUdata mData) {
        if (mSKUListener != null) {
            if (lastResult != mData || lastSelectAll != isSelectAll) {
                lastResult = mData;
                lastSelectAll = isSelectAll;
                mSKUListener.onSelect(isSelectAll, mData);
            }
        }
    }


    private void powerSet(List<SKUdata> list) {
        for (int i = 0; i < list.size(); i++) {
            //已知所求集合的幂集会有2^n个元素
            int size = 2 << list.get(i).getSKUdatas().size();
            List<List<String>> powerSet = new ArrayList<>(size);
            //首先空集肯定是集合的幂集
            powerSet.add(Collections.<String>emptyList());
            if (mSKUResults.size() <= powerSet.size()) {
                mSKUResults.add(new HashMap<String, SKUResult>());
            }
            for (String element : list.get(i).getSKUdatas()) {
                //计算当前元素与已存在幂集的组合
                int preSize = powerSet.size();
                for (int j = 0; j < preSize; j++) {
                    List<String> combineSubset = new ArrayList<>(powerSet.get(j));
                    combineSubset.add(element);
                    powerSet.add(combineSubset);
                    //   Log.e("create", combineSubset.toString());

                    if (mSKUResults.size() <= powerSet.size()) {
                        mSKUResults.add(new HashMap<String, SKUResult>());
                    }
                    mSKUResults.get(combineSubset.size()).put(combineSubset.toString(), new SKUResult(list.get(i), combineSubset));
                }
            }
        }
        /*for (int i1 = 0; i1 < mSKUResults.size(); i1++) {
            for (String key : mSKUResults.get(i1).keySet()) {
                Log.e("ReadALL", mSKUResults.get(i1).get(key).mResults.toString());
            }
        }*/
    }

    public interface SKUListener {
        /**
         * 选择监听
         *
         * @param isSelectAll 是否选择完
         * @param udata       选择数据，isSelectAll为false时，只要用户对第0项数据进行了操作，都回回调此方法
         */
        void onSelect(boolean isSelectAll, SKUdata udata);
    }

}
