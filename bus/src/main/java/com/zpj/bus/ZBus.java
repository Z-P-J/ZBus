package com.zpj.bus;

import android.arch.lifecycle.EventLiveData;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The event bus by {@link EventLiveData}, which can perception lifecycle and support sticky event.
 */
public final class ZBus {

    private static final class CommonBusHolder {
        private static final CommonEventBus BUS = new CommonEventBus();
    }

    private static final class StickyBusHolder {
        private static final StickyEventBus BUS = new StickyEventBus();
    }

    private ZBus() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public static BusObserverBuilder with() {
        return new BusObserverBuilder();
    }

    @Deprecated
    public static BusObserverBuilder with(Object tag) {
        return new BusObserverBuilder(tag);
    }

    public static BusObserverBuilder with(LifecycleOwner owner) {
        return new BusObserverBuilder(owner);
    }

    public static BusObserverBuilder with(View view) {
        return new BusObserverBuilder(view);
    }

    //---------------------------------------------------------------post Event-----------------------------------------------------------

    private static void post(Object o, boolean isSticky) {
        if (isSticky) {
            StickyBusHolder.BUS.post(o);
        } else {
            CommonBusHolder.BUS.post(o);
        }
    }

    public static void post(Object o) {
        post(o, false);
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
        post(o, true);
    }

    public static void postSticky(String key, Object o) {
        postSticky(new MultiEvent(key, o));
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
        Schedulers.getMainHandler().postDelayed(action, delay);
    }


    //--------------------------------------------------------------Observer-------------------------------------------------------------

    public static <T> BusObserver<Consumer<? super T>> observe(@NonNull LifecycleOwner o, @NonNull Class<T> type) {
        return with(o).observe(type);
    }

    public static BusObserver<Consumer<? super String>> observe(@NonNull LifecycleOwner o, @NonNull String key) {
        return with(o).observe(key);
    }

    public static <T> BusObserver<SingleConsumer<? super T>> observe(@NonNull LifecycleOwner o, @NonNull String key, @NonNull Class<T> type) {
        return with(o).observe(key, type);
    }

    public static <S, T> BusObserver<PairConsumer<? super S, ? super T>> observe(@NonNull LifecycleOwner o,
                                                                                 @NonNull String key,
                                                                                 @NonNull Class<S> type1,
                                                                                 @NonNull Class<T> type2) {
        return with(o).observe(key, type1, type2);
    }

    public static <R, S, T> BusObserver<TripleConsumer<? super R, ? super S, ? super T>> observe(@NonNull LifecycleOwner o,
                                                                                                 @NonNull String key,
                                                                                                 @NonNull Class<R> type1,
                                                                                                 @NonNull Class<S> type2,
                                                                                                 @NonNull Class<T> type3) {
        return with(o).observe(key, type1, type2, type3);
    }

    public static <T> BusObserver<Consumer<? super T>> observeSticky(@NonNull LifecycleOwner o,
                                                                     @NonNull Class<T> type) {
        return with(o).observeSticky(type);
    }

    public static BusObserver<Consumer<? super String>> observeSticky(@NonNull LifecycleOwner o,
                                                                      @NonNull String key) {
        return with(o).observeSticky(key);
    }

    public static <T> BusObserver<SingleConsumer<? super T>> observeSticky(@NonNull LifecycleOwner o,
                                                                           @NonNull String key,
                                                                           @NonNull Class<T> type) {
        return with(o).observeSticky(key, type);
    }

    public static <S, T> BusObserver<PairConsumer<S, T>> observeSticky(@NonNull LifecycleOwner o,
                                                                       @NonNull String key,
                                                                       @NonNull Class<S> type1,
                                                                       @NonNull Class<T> type2) {
        return with(o).observeSticky(key, type1, type2);
    }

    public static <R, S, T> BusObserver<TripleConsumer<R, S, T>> observeSticky(@NonNull LifecycleOwner o,
                                                                               @NonNull String key,
                                                                               @NonNull Class<R> type1,
                                                                               @NonNull Class<S> type2,
                                                                               @NonNull Class<T> type3) {
        return with(o).observeSticky(key, type1, type2, type3);
    }

    public static <T> T removeStickyEvent(Class<T> eventType) {
        return StickyBusHolder.BUS.removeStickyEvent(eventType);
    }

    public static Object removeStickyEvent(String key) {
        return StickyBusHolder.BUS.removeStickyEvent(key);
    }

    public static <T> T removeStickyEvent(String key, Class<T> type) {
        return StickyBusHolder.BUS.removeStickyEvent(key, type);
    }

    public static void removeAllStickyEvents() {
        StickyBusHolder.BUS.removeAllStickyEvents();
    }

    public static <T> T getStickyEvent(Class<T> event) {
        return StickyBusHolder.BUS.getStickyEvent(event);
    }

    public static Object getStickyEvent(String key) {
        return StickyBusHolder.BUS.getStickyEvent(key);
    }

    public static <T> T getStickyEvent(String key, Class<T> type) {
        return StickyBusHolder.BUS.getStickyEvent(key, type);
    }


    //---------------------------------------------------------------其他-----------------------------------------------------------------

    public static void removeObservers(Object o) {
        CommonBusHolder.BUS.removeObservers(o);
        StickyBusHolder.BUS.removeObservers(o);
    }

    private abstract static class EventBus {
        protected final Map<Class<?>, EventLiveData<?>> classEventLiveDataMap = new HashMap<>();
        protected final Map<String, EventLiveData<String>> keyEventLiveDataMap = new HashMap<>();
        protected final Map<MultiKey, EventLiveData<MultiEvent>> multiEventLiveDataMap = new HashMap<>();

        protected abstract void post(Object o);

        protected <T> EventLiveData<T> getLiveData(final Class<T> type, boolean isSticky) {
            synchronized (classEventLiveDataMap) {
                EventLiveData<?> liveData = classEventLiveDataMap.get(type);
                if (liveData == null) {
                    liveData = new EventLiveData<>(isSticky);
                    classEventLiveDataMap.put(type, liveData);
                }
                return (EventLiveData<T>) liveData;
            }
        }

        protected EventLiveData<String> getLiveData(final String key, boolean isSticky) {
            synchronized (keyEventLiveDataMap) {
                EventLiveData<String> liveData = keyEventLiveDataMap.get(key);
                if (liveData == null) {
                    liveData = new EventLiveData<>(isSticky);
                    keyEventLiveDataMap.put(key, liveData);
                }
                return liveData;
            }
        }

        protected EventLiveData<MultiEvent> getLiveData(final String key, boolean isStickyEvent, final Class<?>...types) {
            synchronized (multiEventLiveDataMap) {
                MultiKey multiKey = findMultiKey(key, types);
                if (multiKey == null) {
                    multiKey = new MultiKey(key, types);
                    EventLiveData<MultiEvent> liveData = new EventLiveData<>(isStickyEvent);
                    multiEventLiveDataMap.put(multiKey, liveData);
                    return liveData;
                } else {
                    return multiEventLiveDataMap.get(multiKey);
                }
            }
        }

        protected MultiKey findMultiKey(final String key, final Object...objects) {
            synchronized (multiEventLiveDataMap) {
                for (MultiKey multiKey : multiEventLiveDataMap.keySet()) {
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
        }

        protected MultiKey findMultiKey(final String key, final Class<?>...types) {
            synchronized (multiEventLiveDataMap) {
                for (MultiKey multiKey : multiEventLiveDataMap.keySet()) {
                    if (TextUtils.equals(multiKey.getKey(), key)
                            && multiKey.getClasses().length == types.length
                            && Arrays.equals(multiKey.getClasses(), types)) {
                        return multiKey;
                    }
                }
                return null;
            }
        }

        public synchronized void removeObservers(Object o) {
            if (o == null) {
                return;
            }
            synchronized (keyEventLiveDataMap) {
                if (o instanceof String) {
                    EventLiveData<String> liveData = keyEventLiveDataMap.remove(o);
                    if (liveData != null) {
                        liveData.removeObservers();
                    }
                }
                for (EventLiveData<?> liveData : keyEventLiveDataMap.values()) {
                    if (liveData != null) {
                        liveData.removeObservers(o);
                    }
                }
            }

            synchronized (classEventLiveDataMap) {
                for (EventLiveData<?> liveData : classEventLiveDataMap.values()) {
                    if (liveData != null) {
                        liveData.removeObservers(o);
                    }
                }
            }

            synchronized (multiEventLiveDataMap) {
                for (EventLiveData<?> liveData : multiEventLiveDataMap.values()) {
                    if (liveData != null) {
                        liveData.removeObservers(o);
                    }
                }
            }

        }

    }

    private static final class CommonEventBus extends EventBus {

        @Override
        protected void post(Object o) {
            EventLiveData liveData = null;
            if (o instanceof MultiEvent) {
                synchronized (multiEventLiveDataMap) {
                    MultiKey key = findMultiKey(((MultiEvent) o).getKey(), ((MultiEvent) o).getObjects());
                    if (key != null) {
                        liveData = multiEventLiveDataMap.get(key);
                    }
                }
            } else if (o instanceof String) {
                synchronized (keyEventLiveDataMap) {
                    liveData = keyEventLiveDataMap.get(o);
                }
            } else if (o != null) {
                synchronized (classEventLiveDataMap) {
                    liveData = classEventLiveDataMap.get(o.getClass());
                }
            }
            if (liveData != null) {
                liveData.postValue(o);
            }
        }
    }

    private static final class StickyEventBus extends EventBus {

        @Override
        protected void post(Object o) {
            EventLiveData liveData = null;
            if (o instanceof MultiEvent) {
                synchronized (multiEventLiveDataMap) {
                    MultiKey key = findMultiKey(((MultiEvent) o).getKey(), ((MultiEvent) o).getObjects());
                    if (key != null) {
                        liveData = multiEventLiveDataMap.get(key);
                    }
                    if (liveData == null) {
                        if (key == null) {
                            key = ((MultiEvent) o).createMultiKey();
                        }
                        EventLiveData<MultiEvent> data = new EventLiveData<>(true);
                        multiEventLiveDataMap.put(key, data);
                        data.postValue((MultiEvent) o);
                        return;
                    }
                }
            } else if (o instanceof String) {
                synchronized (keyEventLiveDataMap) {
                    liveData = keyEventLiveDataMap.get(o);
                    if (liveData == null) {
                        EventLiveData<String> data = new EventLiveData<>(true);
                        keyEventLiveDataMap.put((String) o, data);
                        data.postValue((String) o);
                        return;
                    }
                }
            } else if (o != null) {
                synchronized (classEventLiveDataMap) {
                    liveData = classEventLiveDataMap.get(o.getClass());
                    if (liveData == null) {
                        EventLiveData data = new EventLiveData(true);
                        classEventLiveDataMap.put(o.getClass(), data);
                        data.postValue(o);
                        return;
                    }
                }
            }
            if (liveData != null) {
                liveData.postValue(o);
            }
        }

        public <T> T removeStickyEvent(Class<T> eventType) {
            synchronized (classEventLiveDataMap) {
                EventLiveData<?> liveData = classEventLiveDataMap.remove(eventType);
                if (liveData == null) {
                    return null;
                }
                return eventType.cast(liveData.getValue());
            }
        }

        public Object removeStickyEvent(String key) {
            synchronized (keyEventLiveDataMap) {
                EventLiveData<?> liveData = keyEventLiveDataMap.remove(key);
                if (liveData == null || !liveData.isStickyEvent()) {
                    return null;
                }
                return liveData.getValue();
            }
        }

        public <T> T removeStickyEvent(String key, Class<T> type) {
            synchronized (multiEventLiveDataMap) {
                MultiKey multiKey = new MultiKey(key, type);
                EventLiveData<MultiEvent> liveData = multiEventLiveDataMap.remove(multiKey);
                if (liveData == null || !liveData.isStickyEvent() || liveData.getValue().getObjects().length != 1) {
                    return null;
                }
                return type.cast(liveData.getValue().getObjectAt(0));
            }
        }

        public void removeAllStickyEvents() {
            synchronized (classEventLiveDataMap) {
                for (Map.Entry<Class<?>, EventLiveData<?>> entry : classEventLiveDataMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().isStickyEvent()) {
                        classEventLiveDataMap.remove(entry.getKey());
                    }
                }
            }
            synchronized (keyEventLiveDataMap) {
                for (Map.Entry<String, EventLiveData<String>> entry : keyEventLiveDataMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().isStickyEvent()) {
                        keyEventLiveDataMap.remove(entry.getKey());
                    }
                }
            }

            synchronized (multiEventLiveDataMap) {
                for (Map.Entry<MultiKey, EventLiveData<MultiEvent>> entry : multiEventLiveDataMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().isStickyEvent()) {
                        multiEventLiveDataMap.remove(entry.getKey());
                    }
                }
            }
        }

        public <T> T getStickyEvent(Class<T> event) {
            synchronized (classEventLiveDataMap) {
                EventLiveData<?> liveData = classEventLiveDataMap.get(event);
                if (liveData == null || !liveData.isStickyEvent()) {
                    return null;
                }
                return event.cast(liveData.getValue());
            }
        }

        public Object getStickyEvent(String key) {
            synchronized (keyEventLiveDataMap) {
                EventLiveData<?> liveData = keyEventLiveDataMap.get(key);
                if (liveData == null || !liveData.isStickyEvent()) {
                    return null;
                }
                return liveData.getValue();
            }
        }

        public <T> T getStickyEvent(String key, Class<T> type) {
            synchronized (multiEventLiveDataMap) {
                MultiKey multiKey = new MultiKey(key, type);
                EventLiveData<MultiEvent> liveData = multiEventLiveDataMap.get(multiKey);
                if (liveData == null || !liveData.isStickyEvent() || liveData.getValue().getObjects().length != 1) {
                    return null;
                }
                return type.cast(liveData.getValue().getObjectAt(0));
            }
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof MultiKey) {
                MultiKey multiKey = (MultiKey) o;

                if (Objects.equals(key, multiKey.key)) {
                    return Arrays.equals(classes, multiKey.classes);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, classes);
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

        public Object getObjectAt(int i) {
            return objects[i];
        }

        @NonNull
        public MultiKey createMultiKey() {
            Class<?>[] classes = new Class<?>[objects.length];
            for (int i = 0; i < objects.length; i++) {
                classes[i] = objects[i].getClass();
            }
            return new MultiKey(key, classes);
        }

    }

    /**
     *  The builder of {@link BusObserver}
     */
    public static final class BusObserverBuilder {

        private LifecycleOwner owner;

        private View view;

        private Object tag;

        @Deprecated
        private BusObserverBuilder() {

        }

        @Deprecated
        private BusObserverBuilder(Object tag) {
            this.tag = tag;
        }

        private BusObserverBuilder(LifecycleOwner owner) {
            this.owner = owner;
        }

        private BusObserverBuilder(View view) {
            this.view = view;
        }

        private <O extends BusObserver<?>> O wrapObserver(O observer) {
            if (owner != null) {
                observer.bindToLife(owner);
                owner = null;
            }
            if (view != null) {
                observer.bindView(view);
                view = null;
            }
            if (tag != null) {
                observer.bindTag(tag);
                tag = null;
            }
            return observer;
        }

        public <T> BusObserver<Consumer<? super T>> observe(@NonNull Class<T> type) {
            return wrapObserver(new BusEventObserver<>(CommonBusHolder.BUS.getLiveData(type, false)));
        }

        public BusObserver<Consumer<? super String>> observe(@NonNull String key) {
            return wrapObserver(new BusEventObserver<>(CommonBusHolder.BUS.getLiveData(key, false)));
        }

        public <T> BusObserver<SingleConsumer<? super T>> observe(@NonNull String key, @NonNull Class<T> type) {
            BusObserver<SingleConsumer<? super T>> observer
                    = new BusEventObserver<>(CommonBusHolder.BUS.getLiveData(key, false, type), key);
            wrapObserver(observer);
            return observer;
        }

        public <S, T> BusObserver<PairConsumer<? super S, ? super T>> observe(@NonNull String key,
                                                                              @NonNull Class<S> type1,
                                                                              @NonNull Class<T> type2) {
            BusObserver<PairConsumer<? super S, ? super T>> observer
                    = new BusEventObserver<>(CommonBusHolder.BUS.getLiveData(key, false, type1, type2), key);
            wrapObserver(observer);
            return observer;
        }

        public <O, P, Q> BusObserver<TripleConsumer<? super O, ? super P, ? super Q>> observe(@NonNull String key,
                                                                                              @NonNull Class<O> type1,
                                                                                              @NonNull Class<P> type2,
                                                                                              @NonNull Class<Q> type3) {
            BusObserver<TripleConsumer<? super O, ? super P, ? super Q>> observer
                    = new BusEventObserver<>(CommonBusHolder.BUS.getLiveData(key, false, type1, type2, type3), key);
            wrapObserver(observer);
            return observer;
        }

        public <T> BusObserver<Consumer<? super T>> observeSticky(@NonNull Class<T> type) {
            BusEventObserver<T, Consumer<? super T>> observer
                    = new BusEventObserver<>(StickyBusHolder.BUS.getLiveData(type, true));
            wrapObserver(observer);
            return observer;
        }

        public BusObserver<Consumer<? super String>> observeSticky(@NonNull String key) {
            BusEventObserver<String, Consumer<? super String>> observer
                    = new BusEventObserver<>(StickyBusHolder.BUS.getLiveData(key, true));
            wrapObserver(observer);
            return observer;
        }

        public <T> BusObserver<SingleConsumer<? super T>> observeSticky(@NonNull String key,
                                                                        @NonNull Class<T> type) {
            BusEventObserver<MultiEvent, SingleConsumer<? super T>> observer
                    = new BusEventObserver<>(StickyBusHolder.BUS.getLiveData(key, true, type), key);
            wrapObserver(observer);
            return observer;
        }

        public <S, T> BusObserver<PairConsumer<S, T>> observeSticky(@NonNull String key,
                                                                    @NonNull Class<S> type1,
                                                                    @NonNull Class<T> type2) {
            BusEventObserver<MultiEvent, PairConsumer<S, T>> observer
                    = new BusEventObserver<>(StickyBusHolder.BUS.getLiveData(key, true, type1, type2), key);
            wrapObserver(observer);
            return observer;
        }

        public <O, P, Q> BusObserver<TripleConsumer<O, P, Q>> observeSticky(@NonNull String key,
                                                                            @NonNull Class<O> type1,
                                                                            @NonNull Class<P> type2,
                                                                            @NonNull Class<Q> type3) {
            BusEventObserver<MultiEvent, TripleConsumer<O, P, Q>> observer
                    = new BusEventObserver<>(StickyBusHolder.BUS.getLiveData(key, true, type1, type2, type3), key);
            wrapObserver(observer);
            return observer;
        }

    }


}
