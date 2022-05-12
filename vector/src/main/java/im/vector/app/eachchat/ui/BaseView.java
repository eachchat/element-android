package im.vector.app.eachchat.ui;

/**
 * Created by zhouguanjie on 2019/8/20.
 */
public interface BaseView<T> {

    public boolean isFinishing();

    public void showToast(String message, boolean isError);

    public void showLoading(String loadingText);

    public void dismissLoading();
}
