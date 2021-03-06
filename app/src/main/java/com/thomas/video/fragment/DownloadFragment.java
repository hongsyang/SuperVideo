package com.thomas.video.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.google.android.material.tabs.TabLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.thomas.core.utils.Utils;
import com.thomas.video.R;
import com.thomas.video.adapter.DownloadAdapter;
import com.thomas.video.base.LazyThomasMvpFragment;
import com.thomas.video.fragment.contract.DownloadContract;
import com.thomas.video.fragment.presenter.DownloadPresenter;
import com.thomas.video.helper.StatusHelper;

import java.util.List;

import butterknife.BindView;

/**
 * @author Thomas
 * @date 2019/6/27
 * @updatelog
 */
public class DownloadFragment extends LazyThomasMvpFragment<DownloadPresenter> implements DownloadContract.View {


    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.smart_refresh_layout)
    SmartRefreshLayout smartRefreshLayout;


    private int type = 0;

    private DownloadAdapter adapter;

    @Override
    protected DownloadPresenter createPresenter() {
        return new DownloadPresenter();
    }

    @Override
    public boolean isNeedRegister() {
        return false;
    }

    @Override
    public void initData(@Nullable Bundle bundle) {
        Aria.download(this).register();
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_download;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        if (holder == null) {
            holder = StatusHelper.getDefault().wrap(smartRefreshLayout).withRetry(() -> {
                holder.showLoading();
                Utils.runOnUiThreadDelayed(() -> presenter.getData(type), 1000);
            });
        }

        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            adapter.getData().clear();
            presenter.getData(type);
        });

        tabLayout.addTab(tabLayout.newTab().setText("正在下载"), 0, true);
        tabLayout.addTab(tabLayout.newTab().setText("下载完成"), 1, false);

        adapter = new DownloadAdapter();
        rvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        rvContent.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                type = tab.getPosition();
                holder.showLoading();
                Utils.runOnUiThreadDelayed(() -> {
                    adapter.getData().clear();
                    presenter.getData(type);
                }, 1000);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                adapter.getData().clear();
                presenter.getData(type);
            }
        });
    }


    @Override
    protected void onFirstUserVisible() {
        holder.showLoading();
        Utils.runOnUiThreadDelayed(() -> {
            adapter.getData().clear();
            presenter.getData(type);
        }, 1000);

    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    public void onFailed(Object tag, String failed) {
        smartRefreshLayout.finishRefresh(false);
        holder.withData(failed).showLoadFailed();
    }

    @Override
    public void getDataSuccess(List<DownloadEntity> succeed) {
        holder.showLoadSuccess();
        smartRefreshLayout.finishRefresh(true);
        adapter.addData(succeed);
    }

    @Override
    public void getDataEmpty() {
        smartRefreshLayout.finishRefresh(true);
        holder.withData("快去下载你想看的影视吧！").showEmpty();
    }
}
