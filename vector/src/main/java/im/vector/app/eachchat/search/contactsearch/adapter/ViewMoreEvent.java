package im.vector.app.eachchat.search.contactsearch.adapter;

/**
 * Created by zhouguanjie on 2020/3/17.
 */
public class ViewMoreEvent {

    private int type;

    private int count;

    private String keyword;

    public ViewMoreEvent(int type, int count, String keyword) {
        this.type = type;
        this.count = count;
        this.keyword = keyword;
    }

    public int getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public String getKeyword() {
        return keyword;
    }
}
