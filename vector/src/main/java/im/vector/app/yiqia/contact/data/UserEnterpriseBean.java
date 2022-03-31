package im.vector.app.yiqia.contact.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chengww on 2019-11-22
 *
 * @author chengww
 */
public class UserEnterpriseBean implements Serializable {

    /**
     * id : 0
     * name : 爱工作科技有限公司4444
     * logoUrl : http://store.jdbmdm.com:8899/group1/M00/00/04/wKgABV3HkwOAepg2AAEa1jzkDDE574.jpg
     * website : https://worklyai.com
     * address : 朝阳区来广营566
     * updateTime : 1574389916456
     */

    private long id;
    private String name;
    private String logoUrl;
    private String website;
    private String address;
    private long updateTime;
    private int recentContactsNumber;
    private long voiceMaxDuration;
    private long lostConnDuration;
    private List<String> statusDescription;

    public long getLostConnDuration() {
        return lostConnDuration;
    }

    public void setLostConnDuration(long lostConnDuration) {
        this.lostConnDuration = lostConnDuration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getRecentContactsNumber() {
        return recentContactsNumber;
    }

    public void setRecentContactsNumber(int recentContactsNumber) {
        this.recentContactsNumber = recentContactsNumber;
    }

    public long getVoiceMaxDuration() {
        return voiceMaxDuration;
    }

    public void setVoiceMaxDuration(long voiceMaxDuration) {
        this.voiceMaxDuration = voiceMaxDuration;
    }

    public List<String> getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(List<String> statusDescription) {
        this.statusDescription = statusDescription;
    }
}
