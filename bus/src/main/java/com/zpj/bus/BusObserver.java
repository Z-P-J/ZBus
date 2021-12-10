package com.zpj.bus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

public interface BusObserver<T> {

//    IObserver<T> subscribeOn(Scheduler scheduler);
//
//    IObserver<T> observeOn(Scheduler scheduler);

    BusObserver<T> bindTag(Object tag);

    BusObserver<T> bindTag(Object tag, boolean disposeBefore);

    BusObserver<T> bindView(View view);

    BusObserver<T> bindToLife(LifecycleOwner lifecycleOwner);

    BusObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event);

    BusObserver<T> doOnAttach(final Runnable onAttach);

    BusObserver<T> doOnChange(final T onChange);

    BusObserver<T> doOnDetach(final Runnable onDetach);

    @Deprecated
    void subscribe(final T onNext);

    void subscribe();

}
