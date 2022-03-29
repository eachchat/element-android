package im.vector.app.yiqia.contact.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhouguanjie on 2019/8/28.
 */
public class Address implements Parcelable {

    private String streetAddress;

    private String locality;

    private String region;

    private String postalCode;

    private String country;

    private String formatted;

    private String type;

    private boolean primary;

    public Address() {}

    protected Address(Parcel in) {
        streetAddress = in.readString();
        locality = in.readString();
        region = in.readString();
        postalCode = in.readString();
        country = in.readString();
        formatted = in.readString();
        type = in.readString();
        primary = in.readByte() != 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFormatted() {
        return formatted;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(streetAddress);
        dest.writeString(locality);
        dest.writeString(region);
        dest.writeString(postalCode);
        dest.writeString(country);
        dest.writeString(formatted);
        dest.writeString(type);
        dest.writeByte((byte) (primary ? 1 : 0));
    }
}
