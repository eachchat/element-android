package im.vector.app.eachchat.search.contactsearch.adapter;

/**
 * Created by zhouguanjie on 2020/3/17.
 */
public class SearchGroupMessageEvent {

    private String groupId, keyWord;

    public SearchGroupMessageEvent(String groupId, String keyWord) {
        this.groupId = groupId;
        this.keyWord = keyWord;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getKeyWord() {
        return keyWord;
    }
}
