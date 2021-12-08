package com.zpj.bus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.Nullable;
import android.view.View;

class LiveDataObserver<T> implements IObserver<Consumer<? super T>>, EventObserver<T> {

    private final LifecycleOwner owner;
    private final ZBus.BusLiveData<T> observable;
//    private Scheduler subscribeScheduler = Schedulers.io();
//    private Scheduler observeScheduler = AndroidSchedulers.mainThread();

    private Consumer<? super T> onNext = null;
    private Runnable onAttach = null;
    private Runnable onDetach = null;

    LiveDataObserver(LifecycleOwner owner, ZBus.BusLiveData<T> observable) {
        this.owner = owner;
        this.observable = observable;
    }

//    @Override
//    public IObserver<Consumer<? super T>> subscribeOn(Scheduler scheduler) {
//        this.subscribeScheduler = scheduler;
//        return this;
//    }
//
//    @Override
//    public IObserver<Consumer<? super T>> observeOn(Scheduler scheduler) {
//        this.observeScheduler = scheduler;
//        return this;
//    }

    @Override
    public IObserver<Consumer<? super T>> bindTag(Object tag) {
        return bindTag(tag, false);
    }

    @Override
    public IObserver<Consumer<? super T>> bindTag(Object tag, boolean disposeBefore) {
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindView(View view) {
        if (view != null) {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    observable.removeObserver(LiveDataObserver.this);
                }
            });
        }
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindToLife(LifecycleOwner lifecycleOwner) {
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnAttach(Runnable onAttach) {
        this.onAttach = onAttach;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnChange(Consumer<? super T> onChange) {
        this.onNext = onChange;
        return this;
    }

    @Override
    public IObserver<Consumer<? super T>> doOnDetach(Runnable onDetach) {
        this.onDetach = onDetach;
        return this;
    }

    @Override
    public void subscribe(Consumer<? super T> onNext) {
        doOnChange(onNext).subscribe();
    }

    @Override
    public void subscribe() {
//        if (subscribeScheduler == null) {
//            subscribeScheduler = Schedulers.io();
//        }
//        if (observeScheduler == null) {
//            observeScheduler = AndroidSchedulers.mainThread();
//        }

        this.observable.observe(owner, this);
    }

    @Override
    public void onAttach() {
        if (onAttach != null) {
            onAttach.run();
        }
    }

    @Override
    public void onDetach() {
        if (onDetach != null) {
            onDetach.run();
        }
    }

    @Override
    public void onChanged(@Nullable T t) {
        if (onNext != null) {
            onNext.accept(t);
        }
    }
}