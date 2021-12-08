package com.zpj.bus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

public interface IObserver<T> {

//    IObserver<T> subscribeOn(Scheduler scheduler);
//
//    IObserver<T> observeOn(Scheduler scheduler);

    IObserver<T> bindTag(Object tag);

    IObserver<T> bindTag(Object tag, boolean disposeBefore);

    IObserver<T> bindView(View view);

    IObserver<T> bindToLife(LifecycleOwner lifecycleOwner);

    IObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event);

    IObserver<T> doOnAttach(final Runnable onAttach);

    IObserver<T> doOnChange(final T onChange);

    IObserver<T> doOnDetach(final Runnable onDetach);

    @Deprecated
    void subscribe(final T onNext);

    void subscribe();

}
