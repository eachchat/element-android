package im.vector.app.yiqia.department.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import im.vector.app.R;
import im.vector.app.yiqia.department.ContactUtils;
import im.vector.app.yiqia.department.DepartmentActivity;
import im.vector.app.yiqia.department.DepartmentFragment;
import im.vector.app.yiqia.department.data.DepartmentBean;
import im.vector.app.yiqia.department.data.IDisplayBean;

/**
 * Created by zhouguanjie on 2019/9/4.
 */
public class OrgHolder extends RecyclerView.ViewHolder {

    TextView mOrgNameTV;

    ImageView mCheckView;

    ImageView mIconView;

    View itemView;

    View bottomLine;

    public OrgHolder(@NonNull View view) {
        super(view);
        itemView = view;
        mOrgNameTV = itemView.findViewById(R.id.org_name);
        mCheckView = itemView.findViewById(R.id.check_view);
        mIconView = itemView.findViewById(R.id.department_iv);
        bottomLine = itemView.findViewById(R.id.view_bottom_line);
    }

    public void bindView(Context context, IDisplayBean department, boolean showBottomLine) {
        bindView(context, department, showBottomLine, null);
    }

    public void bindView(Context context, IDisplayBean department, boolean showBottomLine, View.OnClickListener listener) {
        if (department == null) {
            return;
        }
        if (showBottomLine) {
            bottomLine.setVisibility(View.VISIBLE);
        } else {
            bottomLine.setVisibility(View.GONE);
        }
        String departmentText;
        if (TextUtils.isEmpty(department.getId()) || DepartmentFragment.ROOT_ID.equals(department.getId())) {
            departmentText = context.getString(R.string.organization);
            mIconView.setImageResource(R.mipmap.icon_contacts_org);
            // FileInfoHelper.loadIcon(mIconView, R.mipmap.icon_contacts_org);
        } else if (TextUtils.equals(DepartmentFragment.GROUP_CHAT_ID, department.getId())){
            departmentText = context.getString(R.string.title_group_chat);
            mIconView.setImageResource(R.mipmap.icon_contacts_group_chat);
            // FileInfoHelper.loadIcon(mIconView, R.mipmap.icon_contacts_group_chat);
        } else {
            departmentText = department.getMainContent();
            if (department instanceof DepartmentBean && ((DepartmentBean) department).getDepartmentType().equals("company")) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageResource(ContactUtils.getDepartmentIcon(department));
                // FileInfoHelper.loadIcon(mIconView, ContactUtils.getDepartmentIcon(department));
            } else {
                mIconView.setVisibility(View.GONE);
                mOrgNameTV.setPadding(SizeUtils.dp2px(16f), 0, 0, 0);
            }
        }
        mOrgNameTV.setText(departmentText);
        if (listener != null) {
            itemView.setTag(department);
            itemView.setOnClickListener(listener);
        } else {
            itemView.setOnClickListener(v ->
                    DepartmentActivity.Companion.start(context, department.getMainContent(), department.getId()));
        }
//        mTitle.setVisibility(showHeader ? View.VISIBLE : View.GONE);
    }

    public void setCheckView(boolean canCheck, boolean isChecked) {
        if (!canCheck) {
            mCheckView.setImageResource(R.mipmap.select_contacts_disable);
            return;
        }
        mCheckView.setImageResource(isChecked ? R.mipmap.select_contacts_checked : R.mipmap.select_contacts_nocheck);
    }
}
