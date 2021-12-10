package com.zpj.bus;

import android.annotation.SuppressLint;
import android.arch.lifecycle.EventLiveData;
import android.arch.lifecycle.EventObserver;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

class BusEventObserver<T, C extends Consumer<? super T>>
        implements BusObserver<C>, EventObserver<T>, View.OnAttachStateChangeListener {

    @NonNull
    private final LifecycleOwner owner;
    @NonNull
    private final EventLiveData<T> liveData;
//    private Scheduler subscribeScheduler = Schedulers.io();
//    private Scheduler observeScheduler = AndroidSchedulers.mainThread();

    private final Set<LifecycleBoundObserver> lifecycleBoundObservers = new HashSet<>(0);
    private final Set<Object> tagSet = new HashSet<>(0);
    private final WeakHashMap<View, Object> attachViwMap = new WeakHashMap<>();

    private C onChange = null;
    private Runnable onAttach = null;
    private Runnable onDetach = null;

    BusEventObserver(@NonNull LifecycleOwner owner, @NonNull EventLiveData<T> liveData) {
        this(owner, liveData, null);
    }

    BusEventObserver(@NonNull LifecycleOwner owner, @NonNull EventLiveData<T> liveData, String key) {
        this.owner = owner;
        this.liveData = liveData;
        if (!TextUtils.isEmpty(key)) {
            tagSet.add(key);
        }
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
    public BusObserver<C> bindTag(Object tag) {
        return bindTag(tag, false);
    }

    @Override
    public BusObserver<C> bindTag(Object tag, boolean disposeBefore) {
        this.tagSet.add(tag);
        return this;
    }

    @Override
    public BusObserver<C> bindView(View view) {
        if (view != null) {
            if (!attachViwMap.containsKey(view)) {
                view.addOnAttachStateChangeListener(this);
                attachViwMap.put(view, null);
            }
        }
        return this;
    }

    @Override
    public BusObserver<C> bindToLife(LifecycleOwner lifecycleOwner) {
        return bindToLife(lifecycleOwner, Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public BusObserver<C> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        LifecycleBoundObserver observer = new LifecycleBoundObserver(lifecycleOwner, this, event);
        synchronized (lifecycleBoundObservers) {
            if (!lifecycleBoundObservers.contains(observer)) {
                lifecycleBoundObservers.add(observer);
                observer.attach();
            }
        }
        return this;
    }

    @Override
    public BusObserver<C> doOnAttach(Runnable onAttach) {
        this.onAttach = onAttach;
        return this;
    }

    @Override
    public BusObserver<C> doOnChange(C onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    public BusObserver<C> doOnDetach(Runnable onDetach) {
        this.onDetach = onDetach;
        return this;
    }

    @Override
    public void subscribe(C onNext) {
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

        this.liveData.observe(owner, this);
    }

    @Override
    public void onAttach() {
        if (onAttach != null) {
            onAttach.run();
        }
    }

    @Override
    public void onDetach() {
        onAttach = null;
        onChange = null;
        synchronized (attachViwMap) {
            for (View view : attachViwMap.keySet()) {
                if (view != null) {
                    view.removeOnAttachStateChangeListener(this);
                }
            }
            attachViwMap.clear();
        }

        synchronized (lifecycleBoundObservers) {
            for (LifecycleBoundObserver observer : lifecycleBoundObservers) {
                observer.detach();
            }
            lifecycleBoundObservers.clear();
        }

        tagSet.clear();

        if (onDetach != null) {
            onDetach.run();
        }
        onDetach = null;

    }

    @Override
    public boolean hasTag(Object tag) {
        synchronized (tagSet) {
            return tagSet.contains(tag);
        }
    }

    @Override
    public void onChanged(@Nullable T t) {
        if (onChange != null) {
            onChange.accept(t);
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        synchronized (attachViwMap) {
            attachViwMap.remove(v);
        }
        v.removeOnAttachStateChangeListener(this);
        detach();
    }

    private void detach() {
        liveData.removeObserver(this);
    }

    @SuppressLint("RestrictedApi")
    private static final class LifecycleBoundObserver implements GenericLifecycleObserver {

        @NonNull
        private final LifecycleOwner mOwner;

        @NonNull
        private final BusEventObserver<?, ?> mObserver;

        @NonNull
        private final Lifecycle.Event mEvent;

        public LifecycleBoundObserver(@NonNull LifecycleOwner mOwner, @NonNull BusEventObserver<?, ?> mObserver, @NonNull Lifecycle.Event mEvent) {
            this.mOwner = mOwner;
            this.mObserver = mObserver;
            this.mEvent = mEvent;
        }

        private void attach() {
            this.mOwner.getLifecycle().addObserver(this);
        }

        private void detach() {
            this.mOwner.getLifecycle().removeObserver(this);
        }

        @Override
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (event == this.mEvent || event == Lifecycle.Event.ON_DESTROY) {
                this.mObserver.detach();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof LifecycleBoundObserver) {
                LifecycleBoundObserver that = (LifecycleBoundObserver) o;
                return mOwner.equals(that.mOwner)
                        && mObserver.equals(that.mObserver)
                        && mEvent == that.mEvent;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mOwner, mObserver, mEvent);
        }
    }

}