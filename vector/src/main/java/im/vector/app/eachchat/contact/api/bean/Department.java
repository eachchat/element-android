package im.vector.app.eachchat.contact.api.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.*;
import androidx.room.PrimaryKey;

import kotlinx.parcelize.Parcelize;

/**
 * Created by zhouguanjie on 2019/9/4.
 */
@Entity(tableName = "DepartmentStoreHelper")
@Parcelize
public class Department implements Parcelable {

    @PrimaryKey
    @NonNull
    private String id;

    private String displayName;

    private String description;

    private String departmentType;

    private String parentId;

    private String parentName;

    private int del;

    //排序字段
    private int showOrder;

    public Department() {

    }

    public Department(String id, String name) {
        this.id = id;
        this.displayName = name;
    }

    protected Department(Parcel in) {
        id = in.readString();
        displayName = in.readString();
        description = in.readString();
        departmentType = in.readString();
        parentId = in.readString();
        parentName = in.readString();
        del = in.readInt();
        showOrder = in.readInt();
    }

    public static final Creator<Department> CREATOR = new Creator<Department>() {
        @Override
        public Department createFromParcel(Parcel in) {
            return new Department(in);
        }

        @Override
        public Department[] newArray(int size) {
            return new Department[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(String groupType) {
        this.departmentType = groupType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public int getDel() {
        return del;
    }

    public void setDel(int del) {
        this.del = del;
    }

    public int getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(int showOrder) {
        this.showOrder = showOrder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(displayName);
        dest.writeString(description);
        dest.writeString(departmentType);
        dest.writeString(parentId);
        dest.writeString(parentName);
        dest.writeInt(del);
        dest.writeInt(showOrder);
    }
}
