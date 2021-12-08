package com.zpj.bus;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ZBus {

    private final Map<Class<?>, BusLiveData<?>> classBusLiveDataMap = new ConcurrentHashMap<>();
    private final Map<String, BusLiveData<String>> keyBusLiveDataMap = new ConcurrentHashMap<>();
    private final Map<MultiKey, BusLiveData<MultiEvent>> multiBusLiveDataMap = new ConcurrentHashMap<>();

    private final Map<Object, Object> mStickyEventMap;

    private static final class InstanceHolder {
        private static final ZBus INSTANCE = new ZBus();
    }

    static ZBus get() {
        return InstanceHolder.INSTANCE;
    }

    private ZBus() {
        mStickyEventMap = new ConcurrentHashMap<>();
    }

    //---------------------------------------------------------------post Event-----------------------------------------------------------

    public static void post(Object o) {
        BusLiveData liveData = null;
        if (o instanceof MultiEvent) {
            MultiKey key = get().findMultiKey(((MultiEvent) o).getKey(), ((MultiEvent) o).getObjects());
            if (key != null) {
                liveData = get().multiBusLiveDataMap.get(key);
            }
        } else if (o instanceof String) {
            liveData = get().keyBusLiveDataMap.get(o);
        } else if (o != null) {
            liveData = get().classBusLiveDataMap.get(o.getClass());
        }
        if (liveData != null) {
            liveData.postValue(o);
        }
    }

    public static void post(String key, Object o) {
        post(new MultiEvent(key, o));
    }

    public static void post(String key, Object s, Object t) {
        post(new MultiEvent(key, s, t));
    }

    public static void post(String key, Object r, Object s, Object t) {
        post(new MultiEvent(key, r, s, t));
    }

    public static void postDelayed(final Object o, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                post(o);
            }
        }, delay);
    }

    public static void postDelayed(final String key, final Object o, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                post(key, o);
            }
        }, delay);
    }

    public static void postDelayed(final String key, final Object s, final Object t, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                post(key, s, t);
            }
        }, delay);
    }

    public static void postDelayed(final String key, final Object r, final Object s, final Object t, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                post(new MultiEvent(key, r, s, t));
            }
        }, delay);
    }


    public static void postSticky(Object o) {
        synchronized (get().mStickyEventMap) {
            get().mStickyEventMap.put(o.getClass(), o);
        }
        post(o);
    }

    public static void postSticky(String key, Object o) {
        synchronized (get().mStickyEventMap) {
            get().mStickyEventMap.put(key, new MultiEvent(key, o));
        }
        post(o);
    }

    public static void postSticky(String key, Object s, Object t) {
        postSticky(key, new MultiEvent(key, s, t));
    }

    public static void postSticky(String key, Object r, Object s, Object t) {
        postSticky(new MultiEvent(key, r, s, t));
    }

    public static void postStickyDelayed(final Object o, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                postSticky(o);
            }
        }, delay);
    }

    public static void postStickyDelayed(final String key, final Object o, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                postSticky(key, o);
            }
        }, delay);
    }

    public static void postStickyDelayed(final String key, final Object s, final Object t, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                postSticky(key, s, t);
            }
        }, delay);
    }

    public static void postStickyDelayed(final String key, final Object r, final Object s, final Object t, long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                postSticky(key, r, s, t);
            }
        }, delay);
    }


    public static void postDelayed(Runnable action, long delay) {

    }


    //--------------------------------------------------------------Observer-------------------------------------------------------------

    public static <T> IObserver<Consumer<? super T>> observe(@NonNull LifecycleOwner o, @NonNull Class<T> type) {
        return new LiveDataObserver<>(o, get().getLiveData(type));
    }

    public static IObserver<Consumer<? super String>> observe(@NonNull LifecycleOwner o, @NonNull String key) {
        return new LiveDataObserver<>(o, get().getLiveData(key));
    }

    public static <T> IObserver<SingleConsumer<? super T>> observe(@NonNull LifecycleOwner o, @NonNull String key, @NonNull Class<T> type) {
        return new MultiLiveDataObserver<>(new LiveDataObserver<>(o, get().getLiveData(key, type)));
    }

    public static <S, T> IObserver<PairConsumer<? super S, ? super T>> observe(@NonNull LifecycleOwner o,
                                                               @NonNull String key,
                                                               @NonNull Class<S> type1,
                                                               @NonNull Class<T> type2) {
        return new MultiLiveDataObserver<>(new LiveDataObserver<>(o, get().getLiveData(key, type1, type2)));
    }

    public static <R, S, T> IObserver<TripleConsumer<? super R, ? super S, ? super T>> observe(@NonNull LifecycleOwner o,
                                                                       @NonNull String key,
                                                                       @NonNull Class<R> type1,
                                                                       @NonNull Class<S> type2,
                                                                       @NonNull Class<T> type3) {
        return new MultiLiveDataObserver<>(new LiveDataObserver<>(o, get().getLiveData(key, type1, type2, type3)));
    }

    private <T> BusLiveData<T> getLiveData(final Class<T> type) {
        BusLiveData<?> liveData = classBusLiveDataMap.get(type);
        if (liveData == null) {
            liveData = new BusLiveData<>();
            classBusLiveDataMap.put(type, liveData);
        }
        return (BusLiveData<T>) liveData;
    }

    private BusLiveData<String> getLiveData(final String key) {
        BusLiveData<String> liveData = keyBusLiveDataMap.get(key);
        if (liveData == null) {
            liveData = new BusLiveData<>();
            keyBusLiveDataMap.put(key, liveData);
        }
        return liveData;
    }

    private BusLiveData<MultiEvent> getLiveData(final String key, final Class<?>...types) {
        MultiKey multiKey = findMultiKey(key, types);
        if (multiKey == null) {
            multiKey = new MultiKey(key, types);
            BusLiveData<MultiEvent> liveData = new BusLiveData<>();
            multiBusLiveDataMap.put(multiKey, liveData);
            return liveData;
        } else {
            return multiBusLiveDataMap.get(multiKey);
        }
    }

    private MultiKey findMultiKey(final String key, final Object...objects) {
        for (MultiKey multiKey : multiBusLiveDataMap.keySet()) {
            boolean isSameKey = TextUtils.equals(multiKey.getKey(), key)
                    && multiKey.getClasses().length == objects.length;
            if (isSameKey) {
                for (int i = 0; i < objects.length; i++) {
                    isSameKey = multiKey.getClassAt(i).isInstance(objects[i]);
                    if (!isSameKey) {
                        break;
                    }
                }
                if (isSameKey) {
                    return multiKey;
                }
            }
        }
        return null;
    }

    private MultiKey findMultiKey(final String key, final Class<?>...types) {
        for (MultiKey multiKey : multiBusLiveDataMap.keySet()) {
            boolean isSameKey = TextUtils.equals(multiKey.getKey(), key)
                    && multiKey.getClasses().length == types.length
                    && Arrays.equals(multiKey.getClasses(), types);
            if (isSameKey) {
                return multiKey;
            }
        }
        return null;
    }


    //------------------------------------------------------------------Sticky Event相关-----------------------------------------------------

    public static <T> IObserver<Consumer<? super T>> observeSticky(@NonNull LifecycleOwner o,
                                                                   @NonNull Class<T> type) {
        return new LiveDataObserver<>(o, get().toObservableSticky(type));
    }

    public static IObserver<Consumer<? super String>> observeSticky(@NonNull LifecycleOwner o,
                                                                    @NonNull String key) {
        return new LiveDataObserver<>(o, get().toObservableSticky(key));
    }

    public static <T> IObserver<SingleConsumer<? super T>> observeSticky(@NonNull LifecycleOwner o,
                                                                   @NonNull String key,
                                                                   @NonNull Class<T> type) {
        return new MultiLiveDataObserver<>(new LiveDataObserver<>(o, get().toObservableSticky(key, type)));
    }

    public static <S, T> IObserver<PairConsumer<S, T>> observeSticky(@NonNull LifecycleOwner o,
                                                                     @NonNull String key,
                                                                     @NonNull Class<S> type1,
                                                                     @NonNull Class<T> type2) {
        return new MultiLiveDataObserver<>(new LiveDataObserver<>(o, get().toObservableSticky(key, type1, type2)));
    }

    public static <R, S, T> IObserver<TripleConsumer<R, S, T>> observeSticky(@NonNull LifecycleOwner o,
                                                                             @NonNull String key,
                                                                             @NonNull Class<R> type1,
                                                                             @NonNull Class<S> type2,
                                                                             @NonNull Class<T> type3) {
        return new MultiLiveDataObserver<>(new LiveDataObserver<>(o, get().toObservableSticky(key, type1, type2, type3)));
    }

    private <T> BusLiveData<T> toObservableSticky(final Class<T> eventType) {
//        synchronized (mStickyEventMap) {
//            Observable<T> observable = toObservable(eventType);
//            final Object event = mStickyEventMap.get(eventType);
//
//            if (event != null) {
//                return observable.mergeWith(Observable.just(event).cast(eventType));
//            } else {
//                return observable;
//            }
//        }
        // TODO
        return null;
    }

    private BusLiveData<String> toObservableSticky(final String tag) {
//        synchronized (mStickyEventMap) {
//            Observable<String> observable = getLiveData(tag);
//            final Object event = mStickyEventMap.get(tag);
//
//            if (event != null) {
//                return observable.mergeWith(Observable.just(tag));
//            } else {
//                return observable;
//            }
//        }
        // TODO
        return null;
    }

    private <T> BusLiveData<MultiEvent> toObservableSticky(final String tag, final Class<?>...types) {
//        synchronized (mStickyEventMap) {
//            Observable<RxMultiEvent> observable = toObservable(tag, types);
//            final Object event = mStickyEventMap.get(tag);
//
//            if (event != null) {
//                return observable.mergeWith(Observable.just(event).cast(RxMultiEvent.class));
//            } else {
//                return observable;
//            }
//        }
        // TODO
        return null;
    }

    public static <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (get().mStickyEventMap) {
            return eventType.cast(get().mStickyEventMap.remove(eventType));
        }
    }

    public static Object removeStickyEvent(String key) {
        synchronized (get().mStickyEventMap) {
            return get().mStickyEventMap.remove(key);
        }
    }

    public static <T> T removeStickyEvent(String key, Class<T> type) {
        synchronized (get().mStickyEventMap) {
            return type.cast(get().mStickyEventMap.remove(key));
        }
    }

    public static void removeAllStickyEvents() {
        synchronized (get().mStickyEventMap) {
            get().mStickyEventMap.clear();
        }
    }

    public static <T> T getStickyEvent(Class<T> event) {
        synchronized (get().mStickyEventMap) {
            return event.cast(get().mStickyEventMap.get(event));
        }
    }

    public static Object getStickyEvent(String key) {
        synchronized (get().mStickyEventMap) {
            return get().mStickyEventMap.get(key);
        }
    }

    public static <T> T getStickyEvent(String key, Class<T> event) {
        synchronized (get().mStickyEventMap) {
            return event.cast(get().mStickyEventMap.get(key));
        }
    }


    //---------------------------------------------------------------其他-----------------------------------------------------------------

    public static void removeObservers(Object o) {
        if (o == null) {
            return;
        }
//        RxLife.removeByTag(o);
        // TODO
    }


    //---------------------------------------------------------------Consumer-------------------------------------------------------------

    public abstract static class SingleConsumer<T> implements Consumer<MultiEvent> {

        @Override
        public final void accept(MultiEvent event) {
            onAccept((T) event.getObjects()[0]);
        }

        public abstract void onAccept(T t);
    }

    public abstract static class PairConsumer<S, T> implements Consumer<MultiEvent> {

        @Override
        public final void accept(MultiEvent event) {
            onAccept((S) event.getObjects()[0], (T) event.getObjects()[1]);
        }

        public abstract void onAccept(S s, T t);
    }

    public abstract static class TripleConsumer<R, S, T> implements Consumer<MultiEvent> {

        @Override
        public final void accept(MultiEvent event) {
            R r = (R) event.getObjects()[0];
            S s = (S) event.getObjects()[1];
            T t = (T) event.getObjects()[2];
            onAccept(r, s, t);
        }

        public abstract void onAccept(R r, S s, T t);
    }

    static final class BusLiveData<T> extends MutableLiveData<T> {


        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observe(owner, observer);
            if (observer instanceof EventObserver) {
                ((EventObserver<T>) observer).onAttach();
            }
        }

        @Override
        public void removeObserver(@NonNull Observer<T> observer) {
            super.removeObserver(observer);
            if (observer instanceof EventObserver) {
                ((EventObserver<T>) observer).onDetach();
            }

        }
    }

    private static final class MultiKey {
        private final String key;
        private final Class<?>[] classes;

        MultiKey(String key, Class<?>...classes) {
            this.key = key;
            this.classes = classes;
        }

        public String getKey() {
            return key;
        }

        public Class<?>[] getClasses() {
            return classes;
        }

        public Class<?> getClassAt(int i) {
            return classes[i];
        }
    }

    private static final class MultiEvent {

        private final String key;
        private final Object[] objects;

        MultiEvent(String key, Object...objects) {
            this.key = key;
            this.objects = objects;
        }

        public String getKey() {
            return key;
        }

        public Object[] getObjects() {
            return objects;
        }

        public Object getObject(int i) {
            return objects[i];
        }

    }


}
