package com.spark.chiefwallet.app.me.finance.property.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.android.databinding.library.baseAdapters.BR;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.spark.chiefwallet.R;
import com.spark.ucclient.pojo.LockProoertDetailsResult;

import java.util.List;

/**
 * ================================================
 * 作    者：v1ncent
 * 版    本：1.0.0
 * 创建日期：2019/5/31
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class PropertyLockDetailsAdapter extends BaseQuickAdapter<LockProoertDetailsResult.DataBean.RecordsBean, PropertyLockDetailsAdapter.PropertyLockDetailsViewHolder> {
    public PropertyLockDetailsAdapter(@Nullable List<LockProoertDetailsResult.DataBean.RecordsBean> data) {
        super(R.layout.item_property_lock_details, data);
    }

    @Override
    protected void convert(PropertyLockDetailsViewHolder helper, LockProoertDetailsResult.DataBean.RecordsBean item) {
        ViewDataBinding binding = helper.getBinding();
        binding.setVariable(BR.lockPropertyDetailsResult, item);
        binding.executePendingBindings();
    }


    @Override
    protected View getItemView(int layoutResId, ViewGroup parent) {
        ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater, layoutResId, parent, false);
        if (binding == null) {
            return super.getItemView(layoutResId, parent);
        }
        View view = binding.getRoot();
        view.setTag(R.id.BaseQuickAdapter_databinding_support, binding);
        return view;
    }

    public static class PropertyLockDetailsViewHolder extends BaseViewHolder {

        public PropertyLockDetailsViewHolder(View view) {
            super(view);
        }

        public ViewDataBinding getBinding() {
            return (ViewDataBinding) itemView.getTag(R.id.BaseQuickAdapter_databinding_support);
        }
    }
}
