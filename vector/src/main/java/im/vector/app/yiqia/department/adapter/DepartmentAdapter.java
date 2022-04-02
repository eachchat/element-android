/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.yiqia.department.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import im.vector.app.R;
import im.vector.app.yiqia.contact.api.BaseConstant;
import im.vector.app.yiqia.contact.api.bean.Department;
import im.vector.app.yiqia.contact.data.User;
import im.vector.app.yiqia.department.ContactsHeaderHolder;
import im.vector.app.yiqia.department.data.IDisplayBean;
import im.vector.app.yiqia.ui.stickyHeader.StickyHeaderAdapter;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public class DepartmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderAdapter<ContactsHeaderHolder> {

    private List<IDisplayBean> departments;

    private Context mContext;

    private View.OnClickListener mListener;

    private boolean isAllUser;

    private int showMembersTagPos;

    private boolean withDataBindingHeader;
    // private ContactsViewModel vm;
    private static final int TYPE_HEADER = 10000000;

    public DepartmentAdapter(Context context) {
        this.mContext = context;
    }

    public DepartmentAdapter(Context context, ViewModelStoreOwner owner, LifecycleOwner lifecycleOwner) {
        this.mContext = context;
        if (owner != null) {
            withDataBindingHeader = true;
//            vm = new ViewModelProvider(owner,
//                    new ContactsViewModel.Factory(BaseModule.getMatrixHolder().getSession(),
//                            AppDatabase.getInstance(context).roomInviteDao()))
//                    .get(ContactsViewModel.class);
//            vm.getSize().observe(lifecycleOwner, integer -> notifyItemChanged(0));
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mListener = listener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (withDataBindingHeader && viewType == TYPE_HEADER) {
//            // HeaderInviteBinding inviteBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.header_invite, parent, false);
//            return new RecyclerView.ViewHolder(inviteBinding.getRoot()) {
//            };
//        } else
            if (viewType == BaseConstant.DEPARTMENT_TYPE) {
            return new OrgHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.org_list_item, parent, false));
        } else {
            return new ContactHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contacts_list_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
//        if (getItemViewType(pos) == TYPE_HEADER) {
//            // HeaderInviteBinding binding = DataBindingUtil.getBinding(holder.itemView);
//            // if (binding == null) return;
////            binding.setVariable(BR.vm, vm);
//            binding.executePendingBindings();
//            holder.itemView.setOnClickListener(v -> Navigation.INSTANCE.navigationTo(Contact.RoomInviteActivity));
//            return;
//        }
        int position = withDataBindingHeader ? pos - 1 : pos;
        IDisplayBean department = departments.get(position);

        boolean showBottomLine = false;
        if (department instanceof Department && departments.size() > position + 1) {
            if (position == 0) showBottomLine = true;
            else {
                IDisplayBean nextDepartment = departments.get(position + 1);
                showBottomLine = nextDepartment instanceof Department;
            }
        }

        if (holder instanceof OrgHolder) {
            ((OrgHolder) holder).bindView(mContext, department, showBottomLine, mListener);
        } else if (holder instanceof ContactHolder) {
            // ((ContactHolder) holder).bindView(mContext, department, false, position == getItemCount() - 1);
            ((ContactHolder) holder).bindView(department, false);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (withDataBindingHeader && position == 0) return TYPE_HEADER;
        int pos = withDataBindingHeader ? position - 1 : position;
        return departments.get(pos).getItemType();
    }

    @Override
    public int getItemCount() {
        int size = departments != null ? departments.size() : 0;
        return withDataBindingHeader ? size + 1 : size;
    }

    public void setDepartments(List<IDisplayBean> departments) {
        this.departments = departments;
        notifyDataSetChanged();
    }

    public void setAllUser(boolean allUser) {
        isAllUser = allUser;
    }

    public int getPosByIndex(char c) {
        if (departments == null) {
            return -1;
        }
        for (int index = 0; index < departments.size(); index++) {
            User u = null;
            if (departments.get(index) instanceof User) {
                u = (User) departments.get(index);
            }
            if (u == null) {
                continue;
            }
            if (c == u.getFirstChar()) {
                return withDataBindingHeader ? index + 1 : index;
            }
        }
        return -1;
    }

    @Override
    public long getHeaderId(int childAdapterPosition) {
//        if (!isAllUser) {
//            return StickyHeaderAdapter.NO_HEADER;
//        }
        if (departments == null) return StickyHeaderAdapter.NO_HEADER;
        int pos = withDataBindingHeader ? childAdapterPosition - 1 : childAdapterPosition;
        if (pos < 0) return StickyHeaderAdapter.NO_HEADER;
        if (departments.get(pos) instanceof User) {
            User user = (User) departments.get(pos);
            return user.getFirstChar();
        }
        return StickyHeaderAdapter.NO_HEADER;
    }

    @Override
    public ContactsHeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new ContactsHeaderHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_header_item, parent, false));
    }

    @Override
    public void onBindHeaderViewHolder(ContactsHeaderHolder holder, int childAdapterPosition) {
        int pos = withDataBindingHeader ? childAdapterPosition - 1 : childAdapterPosition;
        if (pos < 0) return;
        if (departments.get(pos) instanceof User) {
            User user = (User) departments.get(pos);
            holder.mHeaderTV.setText(String.valueOf(user.getFirstChar()));
        } else {
            holder.mHeaderTV.setText("");
        }
    }

    public void setShowMembersTagPos(int showMembersTagPos) {
        this.showMembersTagPos = showMembersTagPos;
    }
}
