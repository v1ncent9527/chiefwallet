package com.spark.chiefwallet.app.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.gongwen.marqueen.MarqueeFactory;
import com.gongwen.marqueen.util.OnItemClickListener;
import com.google.common.collect.Lists;
import com.spark.chiefwallet.App;
import com.spark.chiefwallet.BR;
import com.spark.chiefwallet.R;
import com.spark.chiefwallet.api.pojo.AnnounceBean;
import com.spark.chiefwallet.api.pojo.BannerBean;
import com.spark.chiefwallet.api.pojo.BannerPicBean;
import com.spark.chiefwallet.app.home.otc_quotes.OtcquotesFragment;
import com.spark.chiefwallet.app.home.recommend_coin.RecommendCoinFragment;
import com.spark.chiefwallet.base.ARouterPath;
import com.spark.chiefwallet.bean.AnnounceItemBean;
import com.spark.chiefwallet.databinding.FragmentHomeBinding;
import com.spark.chiefwallet.ui.AnnounceView;
import com.spark.chiefwallet.ui.adapter.SlideTabPagerNoCacheAdapter;
import com.spark.chiefwallet.ui.adapter.SlideTabPagerNormalAdapter;
import com.spark.chiefwallet.ui.toast.Toasty;
import com.spark.chiefwallet.util.GlideImageLoader;
import com.spark.wsclient.pojo.RecommendCoinBean;
import com.youth.banner.listener.OnBannerListener;

import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.DummyPagerTitleView;

import java.util.ArrayList;
import java.util.List;

import me.spark.mvvm.base.BaseFragment;
import me.spark.mvvm.base.Constant;
import me.spark.mvvm.http.impl.OnRequestListener;
import me.spark.mvvm.utils.DateUtils;
import me.spark.mvvm.utils.LanguageSPUtil;
import me.spark.mvvm.utils.LogUtils;

/**
 * ================================================
 * 作    者：v1ncent
 * 版    本：1.0.0
 * 创建日期：2019-08-16
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class HomeFragment extends BaseFragment<FragmentHomeBinding, HomeViewModel> {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<Fragment> mRecommendFragments = new ArrayList<>();
    private String[] mTitles;
    private SlideTabPagerNoCacheAdapter mAdapter;
    private SlideTabPagerNormalAdapter mRecommendAdapter;
    private BannerPicBean bannerPicBean;
    private List<AnnounceItemBean> mAnnounceItemBeans = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private List<RecommendCoinBean.DataBean> mRecommendAllList = new ArrayList<>();
    private List<List<RecommendCoinBean.DataBean>> mRecommendGroupList = new ArrayList<>();

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_home;
    }

    @Override
    public int initVariableId() {
        return BR.homeViewModel;
    }

    @Override
    public void initView() {
        binding.banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                ARouter.getInstance().build(ARouterPath.ACTIVITY_EMEX_WEBDETAILS)
                        .withString("title", App.getInstance().getString(R.string.details))
                        .withString("link", bannerPicBean.getAppCarousel().get(position).getLink())
                        .navigation();
            }
        });

        binding.swipeLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.base));
        binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });

        initTab();
    }

    @Override
    public void initData() {
        binding.swipeLayout.setRefreshing(true);
        viewModel.loadBanner(getContext(), new OnRequestListener<BannerBean>() {
            @Override
            public void onSuccess(BannerBean bannerBean) {
                binding.swipeLayout.setRefreshing(false);
                if (null == bannerBean) return;
                LogUtils.e("getAppCarousel", "{\"appCarousel\":" + bannerBean.getData().getAppCarousel() + "}");
                bannerPicBean = App.gson.fromJson("{\"appCarousel\":" + bannerBean.getData().getAppCarousel() + "}", BannerPicBean.class);
                imageUrls.clear();
                int languageType = LanguageSPUtil.getInstance(getContext()).getSelectLanguage();
                for (int i = 0; i < bannerPicBean.getAppCarousel().size(); i++) {
                    if (languageType == 0) {
                        for (BannerPicBean.AppCarouselBean.PictureBean pictureBean : bannerPicBean.getAppCarousel().get(i).getPicture()) {
                            if (pictureBean.getLanguage().equals(Constant.LanguageZH)) {
                                imageUrls.add(pictureBean.getUrl());
                            }
                        }
                    } else if (languageType == 1) {
                        for (BannerPicBean.AppCarouselBean.PictureBean pictureBean : bannerPicBean.getAppCarousel().get(i).getPicture()) {
                            if (pictureBean.getLanguage().equals(Constant.LanguageEN)) {
                                imageUrls.add(pictureBean.getUrl());
                            }
                        }
                    }
                }
                binding.banner
                        .setImageLoader(new GlideImageLoader())
                        .setImages(imageUrls)
                        .start();
            }

            @Override
            public void onFail(String message) {
                binding.swipeLayout.setRefreshing(false);
                Toasty.showError(message);
            }
        });

        viewModel.loadAnnounce(getContext(), new OnRequestListener<AnnounceBean>() {
            @Override
            public void onSuccess(final AnnounceBean announceBean) {
                mAnnounceItemBeans.clear();
                for (AnnounceBean.DataBean.RecordsBean record : announceBean.getData().getRecords()) {
                    AnnounceItemBean announceItemBean = new AnnounceItemBean();
                    announceItemBean.setTitle(record.getHeader());
                    announceItemBean.setTime(DateUtils.formatDate("MM-dd", record.getUpdateTime()));
                    mAnnounceItemBeans.add(announceItemBean);
                }
                MarqueeFactory<LinearLayout, AnnounceItemBean> marqueeFactory = new AnnounceView(getContext());
                marqueeFactory.setData(mAnnounceItemBeans);

                binding.announce.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClickListener(View mView, Object mData, int mPosition) {
                        ARouter.getInstance().build(ARouterPath.ACTIVITY_EMEX_WEBDETAILS)
                                .withString("title", App.getInstance().getString(R.string.str_notice_detail))
                                .withString("link", announceBean.getData().getRecords().get(mPosition).getRedirectUrl())
                                .navigation();
                    }
                });
                binding.announce.setMarqueeFactory(marqueeFactory);
                binding.announce.startFlipping();
            }

            @Override
            public void onFail(String message) {
                Toasty.showError(message);
            }
        });


        if (viewModel.isGetRecommendCoin) return;
        viewModel.loadRecommend(new OnRequestListener<RecommendCoinBean>() {
            @Override
            public void onSuccess(RecommendCoinBean recommendCoinBean) {
                initRecommendCoin(recommendCoinBean);
            }

            @Override
            public void onFail(String message) {

            }
        });
    }


    private void initRecommendCoin(RecommendCoinBean recommendCoinBean) {
        if (viewModel.isGetRecommendCoin) return;
        mRecommendAllList.clear();
        mRecommendGroupList.clear();
        mRecommendAllList.addAll(recommendCoinBean.getData());
        mRecommendGroupList.addAll(Lists.partition(mRecommendAllList, 3));
        mRecommendFragments.clear();
        for (List<RecommendCoinBean.DataBean> dataBeans : mRecommendGroupList) {
            mRecommendFragments.add(RecommendCoinFragment.newInstance(App.gson.toJson(dataBeans)));
        }
        mRecommendAdapter = new SlideTabPagerNormalAdapter(getChildFragmentManager(), mRecommendFragments);
        binding.recommendVp.setOffscreenPageLimit(mRecommendGroupList.size() - 1);
        binding.recommendVp.setAdapter(mRecommendAdapter);

        binding.recommendIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_ccc));
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return mRecommendGroupList == null ? 0 : mRecommendGroupList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                return new DummyPagerTitleView(context);
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                float lineHeight = context.getResources().getDimension(R.dimen.small_navigator_height);
                indicator.setLineHeight(lineHeight);
                indicator.setColors(ContextCompat.getColor(getContext(), R.color.base));
                return indicator;
            }
        });
        binding.recommendIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(binding.recommendIndicator, binding.recommendVp);
        viewModel.isGetRecommendCoin = true;
    }

    private void initTab() {
        mTitles = getResources().getStringArray(R.array.home_tab);
        mFragments.add(new OtcquotesFragment());

        mAdapter = new SlideTabPagerNoCacheAdapter(getChildFragmentManager(), mFragments, mTitles);
        binding.tabVp.setOffscreenPageLimit(mTitles.length - 1);
        binding.tabVp.setAdapter(mAdapter);
        binding.quotesTab.setViewPager(binding.tabVp);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.banner.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.banner.stopAutoPlay();
    }
}