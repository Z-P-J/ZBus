package com.zpj.bus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

/**
 * Observer of bus.
 * @param <T> The type of consumer.
 * @author Z-P-J
 */
public interface BusObserver<T> {

//    IObserver<T> subscribeOn(Scheduler scheduler);

    /**
     * {@link com.zpj.bus.Schedulers.Scheduler}
     * @param scheduler
     * @return
     */
    BusObserver<T> observeOn(Schedulers.Scheduler scheduler);

    /**
     * Bind the tag.
     * @param tag The tag of observer.
     * @return this
     */
    BusObserver<T> bindTag(Object tag);

    /**
     * Bind the tag.
     * @param tag The tag of observer.
     * @param disposeBefore Remove the previous observers which bind the {@param tag}
     * @return this
     */
    BusObserver<T> bindTag(Object tag, boolean disposeBefore);

    /**
     * Bind the view.
     * @param view A View
     * @return this
     */
    BusObserver<T> bindView(View view);

    /**
     *
     * @param lifecycleOwner
     * @return this
     */
    BusObserver<T> bindToLife(LifecycleOwner lifecycleOwner);

    /**
     *
     * @param lifecycleOwner
     * @param event
     * @return this
     */
    BusObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event);

    /**
     *
     * @param onAttach
     * @return this
     */
    BusObserver<T> doOnAttach(final Runnable onAttach);

    /**
     *
     * @param onChange
     * @return this
     */
    BusObserver<T> doOnChange(final T onChange);

    /**
     *
     * @param onDetach
     * @return this
     */
    BusObserver<T> doOnDetach(final Runnable onDetach);

    /**
     * Subscribe the observer of event.
     * @param onNext
     */
    void subscribe(final T onNext);

    /**
     * Subscribe the observer of event.
     */
    void subscribe();

}
