package im.vector.app.eachchat.contact.api.bean;

import java.io.Serializable;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class LoginInput implements Serializable {

    private String account;

    private String password;

    private int yqlVerCode;

    private String osType;

    private String model;

    private String deviceId;

    private Identity identity;

    private String desktopType;

    public static class Identity {
        private String type;
        private String value;
        private String code;

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

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public int getYqlVerCode() {
        return yqlVerCode;
    }

    public void setYqlVerCode(int yqlVerCode) {
        this.yqlVerCode = yqlVerCode;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public String getDesktopType() {
        return desktopType;
    }

    public void setDesktopType(String desktopType) {
        this.desktopType = desktopType;
    }
}
