package im.vector.app.eachchat.rx;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by zhouguanjie on 2019/8/29.
 */
public abstract class SimpleObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public abstract void onNext(T t);

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
