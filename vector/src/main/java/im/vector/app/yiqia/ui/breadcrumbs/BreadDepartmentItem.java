package im.vector.app.yiqia.ui.breadcrumbs;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import im.vector.app.yiqia.contact.api.bean.Department;
import im.vector.app.yiqia.ui.breadcrumbs.model.IBreadcrumbItem;

/**
 * Created by zhouguanjie on 2019/10/29.
 */
public class BreadDepartmentItem implements IBreadcrumbItem<Department> {

    private int mSelectedIndex = -1;
    private List<Department> mItems;

    public BreadDepartmentItem(@NonNull List<Department> items) {
        this(items, 0);
    }

    public BreadDepartmentItem(@NonNull List<Department> items, int selectedIndex) {
        if (!items.isEmpty()) {
            this.mItems = items;
            this.mSelectedIndex = selectedIndex;
        } else {
            throw new IllegalArgumentException("Items shouldn\'t be null empty.");
        }
    }

    private BreadDepartmentItem(Parcel in) {
        mSelectedIndex = in.readInt();
        mItems = in.createTypedArrayList(Department.CREATOR);
    }

    public static final Parcelable.Creator<BreadDepartmentItem> CREATOR = new Parcelable.Creator<BreadDepartmentItem>() {
        @Override
        public BreadDepartmentItem createFromParcel(Parcel in) {
            return new BreadDepartmentItem(in);
        }

        @Override
        public BreadDepartmentItem[] newArray(int size) {
            return new BreadDepartmentItem[size];
        }
    };

    @Override
    public void setSelectedItem(@NonNull Department selectedItem) {
        this.mSelectedIndex = mItems.indexOf(selectedItem);
        if (mSelectedIndex == -1) {
            throw new IllegalArgumentException("This item does not exist in items.");
        }
    }

    @Override
    public void setSelectedIndex(int selectedIndex) {
        this.mSelectedIndex = selectedIndex;
    }

    @Override
    public int getSelectedIndex() {
        return this.mSelectedIndex;
    }

    @Override
    public @NonNull
    String getSelectedItem() {
        return this.mItems.get(getSelectedIndex()).getDisplayName();
    }

    @Override
    public boolean hasMoreSelect() {
        return this.mItems.size() > 1;
    }

    @Override
    public void setItems(@NonNull List<Department> items) {
        this.setItems(items, 0);
    }

    @Override
    public void setItems(@NonNull List<Department> items, int selectedIndex) {
        if (!items.isEmpty()) {
            this.mItems = items;
            this.mSelectedIndex = selectedIndex;
        } else {
            throw new IllegalArgumentException("Items shouldn\'t be null empty.");
        }
    }

    @Override
    public @NonNull
    List<Department> getItems() {
        return mItems;
    }

    /**
     * Create a simple BreadcrumbItem with single item
     */
    public static BreadDepartmentItem createItem(@NonNull Department department) {
        List<Department> departments = new ArrayList<>();
        departments.add(department);
        return new BreadDepartmentItem(departments);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSelectedIndex);
        dest.writeTypedList(mItems);
    }

    @NonNull
    @Override
    public Iterator iterator() {
        return mItems.iterator();
    }
}
