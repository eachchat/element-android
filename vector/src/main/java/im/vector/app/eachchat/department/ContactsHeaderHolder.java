package im.vector.app.eachchat.department;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import im.vector.app.R;

/**
 * Created by zhouguanjie on 2019/8/22.
 */
public class ContactsHeaderHolder extends RecyclerView.ViewHolder {

    public TextView mHeaderTV;

    public ContactsHeaderHolder(View view) {
        super(view);
        mHeaderTV = view.findViewById(R.id.tv_header);
    }

}
