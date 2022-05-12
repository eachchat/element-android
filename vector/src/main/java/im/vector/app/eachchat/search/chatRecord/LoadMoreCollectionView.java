package im.vector.app.eachchat.search.chatRecord;

import com.chad.library.adapter.base.loadmore.LoadMoreView;

import im.vector.app.R;

/**
 * Created by zhouguanjie on 2019/12/10.
 */
public class LoadMoreCollectionView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return R.layout.loadmore_view;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view;
    }

    @Override
    protected int getLoadProgressBarViewId() {
        return 0;
    }

    @Override
    protected int getLoadTextViewId() {
        return 0;
    }

    @Override
    protected int getLoadFailPromptViewId() {
        return 0;
    }

    @Override
    protected int getLoadNomoreViewId() {
        return 0;
    }
}
