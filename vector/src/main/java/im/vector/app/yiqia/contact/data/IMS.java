package im.vector.app.yiqia.contact.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhouguanjie on 2019/8/28.
 */
public class IMS implements Parcelable {

    private String value;

    private String type;

    public IMS() {}

    protected IMS(Parcel in) {
        value = in.readString();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IMS> CREATOR = new Creator<IMS>() {
        @Override
        public IMS createFromParcel(Parcel in) {
            return new IMS(in);
        }

        @Override
        public IMS[] newArray(int size) {
            return new IMS[size];
        }
    };

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
