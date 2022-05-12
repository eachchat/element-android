package im.vector.app.eachchat.search.contactsearch.event;

import android.content.Context;

/**
 * Created by zhouguanjie on 2020/2/18.
 */
public class SearchDataEvent {

    private int type;

    private String id;

    private String name;

    private Context context;

    public SearchDataEvent(Context context, int type, String id, String name) {
        this.context = context;
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Context getContext() {
        return context;
    }
}
