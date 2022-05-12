package im.vector.app.eachchat.search.contactsearch.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import im.vector.app.R;
import im.vector.app.eachchat.department.data.IDisplayBean;
import im.vector.app.eachchat.search.contactsearch.data.AppConstant;
import im.vector.app.eachchat.search.contactsearch.data.SearchParam;

/**
 * Created by zhouguanjie on 2019/9/9.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private List<IDisplayBean> mResults;

    private final Activity mContext;

    private String keyWord;

    private final SearchParam param;

    //新版复合搜索
    private boolean isV2;

    public SearchAdapter(Activity context, SearchParam param) {
        this.mContext = context;
        this.param = param;
    }

    public void setV2(boolean v2) {
        isV2 = v2;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(mContext, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false), isV2);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        IDisplayBean search = mResults.get(position);
        IDisplayBean preBean = null;
        IDisplayBean nextBean = null;
        if (position >= 1) {
            preBean = mResults.get(position - 1);
        }
        if (mResults.size() > position + 1) {
            nextBean = mResults.get(position + 1);
        }

        holder.bindView(mContext, position, search, preBean, nextBean, keyWord, param);

        if (param.getSearchType() == AppConstant.SEARCH_MULTI_TYPE) {
            holder.tryShowMoreView();
        } else {
            holder.hideMoreView();
        }
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public boolean isOverMaxShow(int type) {
        if (type != AppConstant.SEARCH_MULTI_TYPE) {
            return false;
        }
        int maxLimitCount = param.getCount();
        int count = 0;
        for (int index = 0; index < mResults.size(); index++) {
            if (count >= maxLimitCount) {
                return true;
            }
            if (type == mResults.get(index).getType()) {
                count++;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return mResults == null ? 0 : mResults.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDatas(List<IDisplayBean> datas) {
        this.mResults = datas;
        notifyDataSetChanged();
    }
}
