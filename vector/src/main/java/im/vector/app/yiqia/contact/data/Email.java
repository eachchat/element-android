package im.vector.app.yiqia.contact.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhouguanjie on 2019/8/28.
 */
public class Email implements Parcelable {

    private String type;

    private String value;

    private boolean primary;

    private String userId;

    public Email() {
    }

    protected Email(Parcel in) {
        type = in.readString();
        value = in.readString();
        primary = in.readByte() != 0;
        userId = in.readString();
    }

    public static final Creator<Email> CREATOR = new Creator<>() {
        @Override
        public Email createFromParcel(Parcel in) {
            return new Email(in);
        }

        @Override
        public Email[] newArray(int size) {
            return new Email[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(value);
        dest.writeByte((byte) (primary ? 1 : 0));
        dest.writeString(userId);
    }
}
