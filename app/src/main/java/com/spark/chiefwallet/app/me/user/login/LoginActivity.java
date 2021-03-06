package com.spark.chiefwallet.app.me.user.login;

import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.spark.chiefwallet.BR;
import com.spark.chiefwallet.R;
import com.spark.chiefwallet.base.ARouterPath;
import com.spark.chiefwallet.bean.TitleBean;
import com.spark.chiefwallet.databinding.ActivityLoginBinding;
import com.spark.chiefwallet.util.StatueBarUtils;

import me.spark.mvvm.base.BaseActivity;

/**
 * ================================================
 * 作    者：v1ncent
 * 版    本：1.0.0
 * 创建日期：2019/4/27
 * 描    述：
 * 修订历史：
 * ================================================
 */
@Route(path = ARouterPath.ACTIVITY_ME_LOGIN)
public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginViewModel> {
    private TitleBean mTitleModel;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_login;
    }

    @Override
    public int initVariableId() {
        return BR.loginViewModel;
    }

    @Override
    public void initView() {
        super.initView();
        StatueBarUtils.setStatusBarLightMode(this, true);
        StatueBarUtils.addMarginTopEqualStatusBarHeight(binding.loginTitle.fakeStatusBar);
        StatueBarUtils.setStatusBarColor(this, Color.WHITE);

        //TitleSet
        mTitleModel = new TitleBean();
        mTitleModel.setShowLeftImg(true);
        mTitleModel.setShowRightTV(true);
        mTitleModel.setRightTV(getString(R.string.register));
        binding.loginTitle.titleLeftImg.setImageDrawable(getResources().getDrawable(R.drawable.svg_cancel));
        binding.loginTitle.setViewTitle(mTitleModel);
        setTitleListener(binding.loginTitle.titleRootLeft, binding.loginTitle.titleRootRight);

        viewModel.initContext(this);
    }

    @Override
    protected void onTitleRightClick() {
        //注册
        ARouter.getInstance().build(ARouterPath.ACTIVITY_ME_REGISTER)
                .navigation();
        finish();
    }

    @Override
    public void initViewObservable() {
        //密码显示开关
        viewModel.uc.pwdSwitchEvent.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                binding.pwdSwitch.setImageDrawable(aBoolean ?
                        getResources().getDrawable(R.drawable.svg_hide) :
                        getResources().getDrawable(R.drawable.svg_show));
                binding.userPassword.setTransformationMethod(aBoolean ?
                        HideReturnsTransformationMethod.getInstance() :
                        PasswordTransformationMethod.getInstance());
                binding.userPassword.setSelection(viewModel.userPassWord.get().length());
            }
        });

        viewModel.uc.smsCodePopopShow.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(aBoolean){
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }else {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            }
        });
    }


}
