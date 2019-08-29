package com.spark.chiefwallet.app.me.finance.property.legal_currency;

import android.app.Application;
import android.content.Context;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.spark.acclient.FinanceClient;
import com.spark.acclient.pojo.SpotWalletResult;
import com.spark.chiefwallet.R;
import com.spark.otcclient.LcTradeClient;
import com.spark.otcclient.pojo.AuthMerchantResult;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;

import me.spark.mvvm.base.BaseViewModel;
import me.spark.mvvm.base.Constant;
import me.spark.mvvm.base.EvKey;
import me.spark.mvvm.binding.command.BindingAction;
import me.spark.mvvm.binding.command.BindingCommand;
import me.spark.mvvm.bus.event.SingleLiveEvent;
import me.spark.mvvm.http.impl.OnRequestListener;
import me.spark.mvvm.utils.DfUtils;
import me.spark.mvvm.utils.EventBean;
import me.spark.mvvm.utils.EventBusUtils;
import me.spark.mvvm.utils.LogUtils;
import me.spark.mvvm.utils.MathUtils;
import me.spark.mvvm.utils.SPUtils;
import me.spark.mvvm.utils.SpanUtils;

/**
 * ================================================
 * 作    者：v1ncent
 * 版    本：1.0.0
 * 创建日期：2019/4/28
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class LegalCurAccountViewModel extends BaseViewModel {
    public LegalCurAccountViewModel(@NonNull Application application) {
        super(application);
    }

    private Context mContext;
    public ObservableField<CharSequence> otcWalletTotalChar = new ObservableField<>();
    public ObservableField<String> otcWalletTransChar = new ObservableField<>();
    public ObservableField<String> marginAmount = new ObservableField<>();
    private OnRequestListener mOnRequestListener;
    private double walletTotal, walletTotalTrans;


    public UIChangeObservable uc = new UIChangeObservable();

    public class UIChangeObservable {
        public SingleLiveEvent<Boolean> isHideAccountSwitchEvent = new SingleLiveEvent<>();
    }

    //资金详情的展示与隐藏
    public BindingCommand hideAccountOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.isHideAccountSwitchEvent.setValue(uc.isHideAccountSwitchEvent.getValue() == null || !uc.isHideAccountSwitchEvent.getValue());
        }
    });

    public void initAccountText(boolean isHide) {
        if (isHide) {
            otcWalletTotalChar.set("****** USDT");
            otcWalletTransChar.set("≈¥ ****");
        } else {
            otcWalletTotalChar.set(initWalletTotal(walletTotal));
            otcWalletTransChar.set(transWalletTotal(walletTotalTrans));
        }
    }

    public void iniOtcWallet(Context context, OnRequestListener mOnRequestListener) {
        this.mContext = context;
        this.mOnRequestListener = mOnRequestListener;
//        LcTradeClient.getInstance().authMerchantFind();
        FinanceClient.getInstance().getCoinWallet("OTC");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBean eventBean) {
        if ((!isVisible2User())
                && (!eventBean.getOrigin().equals(EvKey.walletChange))
                && (!eventBean.getOrigin().equals(EvKey.coinWallet)))
            return;
        switch (eventBean.getOrigin()) {
            //保证金
            case EvKey.authMerchantFind2:
                if (eventBean.isStatue()) {
                    AuthMerchantResult authMerchantResult = (AuthMerchantResult) eventBean.getObject();
                    marginAmount.set(mContext.getString(R.string.margin) + DfUtils.numberFormat(authMerchantResult.getData().getMargin(), 2) + " " + authMerchantResult.getData().getCoin());
                }
                FinanceClient.getInstance().getCoinWallet("OTC");
                break;
            //otc钱包查询业务处理
            case EvKey.coinWallet:
                if (mOnRequestListener == null || eventBean.getType() != 1) return;
                if (eventBean.isStatue()) {
                    updateSpotInfo((SpotWalletResult) eventBean.getObject());
                } else {
                    mOnRequestListener.onFail(eventBean.getMessage());
                }
                break;
            case EvKey.walletChange:
                LcTradeClient.getInstance().authMerchantFind();
                break;
            case EvKey.logout_success_401:
                if (eventBean.isStatue()) {
                    dismissDialog();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 更新币币资产信息
     *
     * @param spotWalletResult
     */
    private void updateSpotInfo(SpotWalletResult spotWalletResult) {
        if (mOnRequestListener == null) return;
        walletTotal = 0;
        walletTotalTrans = 0;
        for (SpotWalletResult.DataBean dataBean : spotWalletResult.getData()) {
            walletTotal = new BigDecimal(dataBean.getTotalPlatformAssetBalance()).add(new BigDecimal(walletTotal)).doubleValue();
//            walletTotalTrans = new BigDecimal(dataBean.getCnyAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();
            //1.人民币 CNY 2.美元 USDT 3.欧元 EUR 4.赛地 GHS 5.尼日利亚 NGN
            switch (SPUtils.getInstance().getPricingCurrency()) {
                case "1":
                    walletTotalTrans = new BigDecimal(dataBean.getCnyAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();

                    break;
                case "2":
                    walletTotalTrans = new BigDecimal(dataBean.getUsdtAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();

                    break;
                case "3":
                    walletTotalTrans = new BigDecimal(dataBean.getEurAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();

                    break;
                case "4":
                    walletTotalTrans = new BigDecimal(dataBean.getGhsAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();

                    break;
                case "5":
                    walletTotalTrans = new BigDecimal(dataBean.getNgnAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();

                    break;
                default:
                    walletTotalTrans = new BigDecimal(dataBean.getCnyAssetBalance()).add(new BigDecimal(walletTotalTrans)).doubleValue();
                    break;
            }
        }
        initAccountText(SPUtils.getInstance().isHideAccountOtc());
        mOnRequestListener.onSuccess(spotWalletResult);
    }

    /**
     * 钱包总额
     *
     * @param spotWalletTotal
     * @return
     */
    private CharSequence initWalletTotal(double spotWalletTotal) {
        String close = DfUtils.formatNum(MathUtils.getRundNumber(spotWalletTotal, 6, null));
        if (!close.contains(".")) return close;
        CharSequence text = new SpanUtils()
                .append(close.split("\\.")[0])
                .append("." + close.split("\\.")[1]).setFontSize(16, true)
                .append(" USDT").setFontSize(16, true)
                .create();
        return text;
    }

    private String transWalletTotal(double spotWalletTotal) {
        String close = DfUtils.formatNum(MathUtils.getRundNumber(spotWalletTotal, 6, null));
        //1.人民币 CNY 2.美元 USDT 3.欧元 EUR 4.赛地 GHS 5.尼日利亚 NGN
        if (SPUtils.getInstance().getPricingCurrency().equals("1")) {
            return "≈ " + Constant.CNY_symbol + close;
        } else if (SPUtils.getInstance().getPricingCurrency().equals("2")) {
            return "≈ " + Constant.USD_symbol + close;

        } else if (SPUtils.getInstance().getPricingCurrency().equals("3")) {
            return "≈ " + Constant.EUR_symbol + close ;

        } else if (SPUtils.getInstance().getPricingCurrency().equals("4")) {
            return "≈ " + Constant.GHS_symbol + close;

        } else if (SPUtils.getInstance().getPricingCurrency().equals("5")) {
            return "≈ " + Constant.NGN_symbol + close;
        } else
            return "≈ " + Constant.CNY_symbol + close;
//        return "≈¥ " + close;
    }

    private boolean isVisible2User() {
        return Constant.accountPage == 1;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBusUtils.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBusUtils.unRegister(this);
        LogUtils.e("onStop");
    }
}
