package com.spark.chiefwallet.app.me.safe.safecentre.email;

import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.spark.chiefwallet.BR;
import com.spark.chiefwallet.R;
import com.spark.chiefwallet.base.ARouterPath;
import com.spark.chiefwallet.bean.TitleBean;
import com.spark.chiefwallet.databinding.ActivityEmailBinding;
import com.spark.chiefwallet.ui.toast.Toasty;
import com.spark.chiefwallet.util.RegexUtils;
import com.spark.chiefwallet.util.StatueBarUtils;

import butterknife.OnClick;
import cn.iwgang.countdownview.CountdownView;
import me.spark.mvvm.base.BaseActivity;
import me.spark.mvvm.utils.StringUtils;


/**
 * ================================================
 * 作    者：v1ncent
 * 版    本：1.0.0
 * 创建日期：2019/4/29
 * 描    述：
 * 修订历史：
 * ================================================
 */
@Route(path = ARouterPath.ACTIVITY_ME_SAFECENTRE_EMAIL)
public class EmailActivity extends BaseActivity<ActivityEmailBinding, EmailViewModel> {
    private TitleBean mTitleModel;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_email;
    }

    @Override
    public int initVariableId() {
        return BR.emailViewModel;
    }

    @Override
    public void initView() {
        super.initView();
        StatueBarUtils.setStatusBarLightMode(this, true);
        StatueBarUtils.addMarginTopEqualStatusBarHeight(binding.emailTitle.fakeStatusBar);
        StatueBarUtils.setStatusBarColor(this, Color.WHITE);

        //TitleSet
        mTitleModel = new TitleBean();
        binding.emailTitle.setViewTitle(mTitleModel);
        setTitleListener(binding.emailTitle.titleRootLeft);
    }

    @Override
    public void initViewObservable() {
        viewModel.uc.mGetCodeSuccessLiveEvent.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                binding.getSmsCode.setVisibility(View.INVISIBLE);
                binding.llCountdownView.setVisibility(View.VISIBLE);
                binding.countdownView.start(60 * 1000);
                binding.countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                    @Override
                    public void onEnd(CountdownView cv) {
                        binding.getSmsCode.setVisibility(View.VISIBLE);
                        binding.llCountdownView.setVisibility(View.INVISIBLE);
                    }
                });
                Toasty.showSuccess("获取邮箱验证码成功");
            }
        });
    }

    @OnClick({R.id.get_sms_code})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_sms_code:
                if (StringUtils.isEmpty(viewModel.email.get())) {
                    Toasty.showError(getString(R.string.email_address_hint));
                    return;
                }
                if (!RegexUtils.isEmail(viewModel.email.get())) {
                    Toasty.showError("请输入有效的邮箱地址");
                    return;
                }
                viewModel.getEmailCode(this);
                break;
        }
    }
}
