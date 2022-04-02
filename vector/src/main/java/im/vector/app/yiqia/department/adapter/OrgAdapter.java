package im.vector.app.yiqia.department.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import im.vector.app.R;
import im.vector.app.yiqia.department.data.IDisplayBean;

/**
 * Created by zhouguanjie on 2019/9/4.
 */
public class OrgAdapter extends RecyclerView.Adapter<OrgHolder> {

    private List<IDisplayBean> mDepartments;

    private Context mContext;

    private View.OnClickListener mListener;

    public OrgAdapter(Context context) {
        this.mContext = context;
    }

    public void setListener(View.OnClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public OrgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrgHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.org_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrgHolder holder, int position) {
        IDisplayBean department = mDepartments.get(position);
        holder.mIconView.setImageResource(R.mipmap.org_icon);
        holder.bindView(mContext, department, position == getItemCount() - 1, mListener);
    }

    @Override
    public int getItemCount() {
        return mDepartments == null ? 0 : mDepartments.size();
    }

    public void setDepartments(List<IDisplayBean> departments) {
        this.mDepartments = departments;
        notifyDataSetChanged();
    }
}
