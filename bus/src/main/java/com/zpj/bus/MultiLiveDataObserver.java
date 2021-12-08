package com.zpj.bus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

class MultiLiveDataObserver<S, T extends Consumer<S>>
        implements IObserver<T> {

    protected final LiveDataObserver<S> observer;

    MultiLiveDataObserver(LiveDataObserver<S> observer) {
        this.observer = observer;
    }

//    @Override
//    public IObserver<T> subscribeOn(Scheduler scheduler) {
//        observer.subscribeOn(scheduler);
//        return this;
//    }
//
//    @Override
//    public IObserver<T> observeOn(Scheduler scheduler) {
//        observer.observeOn(scheduler);
//        return this;
//    }

    @Override
    public IObserver<T> bindTag(Object tag) {
        observer.bindTag(tag);
        return this;
    }

    @Override
    public IObserver<T> bindTag(Object tag, boolean disposeBefore) {
        observer.bindTag(tag, disposeBefore);
        return this;
    }

    @Override
    public IObserver<T> bindView(View view) {
        observer.bindView(view);
        return this;
    }

    @Override
    public IObserver<T> bindToLife(LifecycleOwner lifecycleOwner) {
        observer.bindToLife(lifecycleOwner);
        return this;
    }

    @Override
    public IObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        observer.bindToLife(lifecycleOwner, event);
        return this;
    }

    @Override
    public IObserver<T> doOnAttach(Runnable onAttach) {
        observer.doOnAttach(onAttach);
        return this;
    }

    @Override
    public IObserver<T> doOnChange(final T onChange) {
        observer.doOnChange(onChange);
        return this;
    }

    @Override
    public IObserver<T> doOnDetach(final Runnable onDetach) {
        observer.doOnDetach(onDetach);
        return this;
    }

    @Override
    public void subscribe(T onNext) {
        observer.subscribe(onNext);
    }

    @Override
    public void subscribe() {
        observer.subscribe();
    }

}