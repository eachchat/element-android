package im.vector.app.eachchat.ui.breadcrumbs;

import androidx.annotation.NonNull;

/**
 * Created by zhouguanjie on 2019/10/29.
 */
public interface BreadcrumbsCallback<E> {

    /**
     * Called when BreadcrumbsView's item has been clicked
     *
     * @param view     Parent view
     * @param position The position of clicked item
     */
    void onItemClick(@NonNull BreadcrumbsView view, int position);

    /**
     * Called when BreadcrumbsView's item has been changed
     *
     * @param view           Parent view
     * @param parentPosition The position of the parent item of changed item
     * @param nextItem       Next item title
     */
    void onItemChange(@NonNull BreadcrumbsView view, int parentPosition, @NonNull E nextItem);

}
