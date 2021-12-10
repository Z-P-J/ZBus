package com.zpj.bus;

import android.arch.lifecycle.EventLiveData;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ZBus {

    private final Map<Class<?>, BusLiveData<?>> classBusLiveDataMap = new ConcurrentHashMap<>();
    private final Map<String, BusLiveData<String>> keyBusLiveDataMap = new ConcurrentHashMap<>();
    private final Map<MultiKey, BusLiveData<MultiEvent>> multiBusLiveDataMap = new ConcurrentHashMap<>();

    private static final class InstanceHolder {
        private static final ZBus INSTANCE = new ZBus();
    }

    static ZBus get() {
        return InstanceHolder.INSTANCE;
    }

    private ZBus() {

    }

    //---------------------------------------------------------------post Event-----------------------------------------------------------

    private static void post(Object o, boolean isSticky) {
        BusLiveData liveData = null;
        if (o instanceof MultiEvent) {
            MultiKey key = get().findMultiKey(((MultiEvent) o).getKey(), ((MultiEvent) o).getObjects());
            if (key != null) {
                liveData = get().multiBusLiveDataMap.get(key);
            }
            if (liveData == null && isSticky) {
                if (key == null) {
                    key = ((MultiEvent) o).createMultiKey();
                }
                BusLiveData<MultiEvent> data = new BusLiveData<>(isSticky);
                get().multiBusLiveDataMap.put(key, data);
                data.setValue((MultiEvent) o);
                return;
            }
        } else if (o instanceof String) {
            liveData = get().keyBusLiveDataMap.get(o);
            if (liveData == null && isSticky) {
                BusLiveData<String> data = new BusLiveData<>(isSticky);
                get().keyBusLiveDataMap.put((String) o, data);
                data.setValue((String) o);
                return;
            }
        } else if (o != null) {
            liveData = get().classBusLiveDataMap.get(o.getClass());
            if (liveData == null && isSticky) {
                BusLiveData data = new BusLiveData(isSticky);
                get().classBusLiveDataMap.put(o.getClass(), data);
                data.setValue(o);
                return;
            }
        }
        if (liveData != null) {
            liveData.postValue(o);
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

    }


    //--------------------------------------------------------------Observer-------------------------------------------------------------

    public static <T> BusObserver<Consumer<? super T>> observe(@NonNull LifecycleOwner o, @NonNull Class<T> type) {
        return new BusEventObserver<>(o, get().getLiveData(type, false));
    }

    public static BusObserver<Consumer<? super String>> observe(@NonNull LifecycleOwner o, @NonNull String key) {
        return new BusEventObserver<>(o, get().getLiveData(key, false));
    }

    public static <T> BusObserver<SingleConsumer<? super T>> observe(@NonNull LifecycleOwner o, @NonNull String key, @NonNull Class<T> type) {
        return new BusEventObserver<>(o, get().getLiveData(key, false, type), key);
    }

    public static <S, T> BusObserver<PairConsumer<? super S, ? super T>> observe(@NonNull LifecycleOwner o,
                                                                                 @NonNull String key,
                                                                                 @NonNull Class<S> type1,
                                                                                 @NonNull Class<T> type2) {
        return new BusEventObserver<>(o, get().getLiveData(key, false, type1, type2), key);
    }

    public static <R, S, T> BusObserver<TripleConsumer<? super R, ? super S, ? super T>> observe(@NonNull LifecycleOwner o,
                                                                                                 @NonNull String key,
                                                                                                 @NonNull Class<R> type1,
                                                                                                 @NonNull Class<S> type2,
                                                                                                 @NonNull Class<T> type3) {
        return new BusEventObserver<>(o, get().getLiveData(key, false, type1, type2, type3), key);
    }

    private <T> BusLiveData<T> getLiveData(final Class<T> type, boolean isSticky) {
        BusLiveData<?> liveData = classBusLiveDataMap.get(type);
        if (liveData == null) {
            liveData = new BusLiveData<>(isSticky);
            classBusLiveDataMap.put(type, liveData);
        }
        return (BusLiveData<T>) liveData;
    }

    private BusLiveData<String> getLiveData(final String key, boolean isSticky) {
        BusLiveData<String> liveData = keyBusLiveDataMap.get(key);
        if (liveData == null) {
            liveData = new BusLiveData<>(isSticky);
            keyBusLiveDataMap.put(key, liveData);
        }
        return liveData;
    }

    private BusLiveData<MultiEvent> getLiveData(final String key, boolean isStickyEvent, final Class<?>...types) {
        MultiKey multiKey = findMultiKey(key, types);
        if (multiKey == null) {
            multiKey = new MultiKey(key, types);
            BusLiveData<MultiEvent> liveData = new BusLiveData<>(isStickyEvent);
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
            if (TextUtils.equals(multiKey.getKey(), key)
                    && multiKey.getClasses().length == types.length
                    && Arrays.equals(multiKey.getClasses(), types)) {
                return multiKey;
            }
        }
        return null;
    }


    //------------------------------------------------------------------Sticky Event相关-----------------------------------------------------

    public static <T> BusObserver<Consumer<? super T>> observeSticky(@NonNull LifecycleOwner o,
                                                                     @NonNull Class<T> type) {
        return new BusEventObserver<>(o, get().getLiveData(type, true));
    }

    public static BusObserver<Consumer<? super String>> observeSticky(@NonNull LifecycleOwner o,
                                                                      @NonNull String key) {
        return new BusEventObserver<>(o, get().getLiveData(key, true));
    }

    public static <T> BusObserver<SingleConsumer<? super T>> observeSticky(@NonNull LifecycleOwner o,
                                                                           @NonNull String key,
                                                                           @NonNull Class<T> type) {
        return new BusEventObserver<>(o, get().getLiveData(key, true, type), key);
    }

    public static <S, T> BusObserver<PairConsumer<S, T>> observeSticky(@NonNull LifecycleOwner o,
                                                                       @NonNull String key,
                                                                       @NonNull Class<S> type1,
                                                                       @NonNull Class<T> type2) {
        return new BusEventObserver<>(o, get().getLiveData(key, true, type1, type2), key);
    }

    public static <R, S, T> BusObserver<TripleConsumer<R, S, T>> observeSticky(@NonNull LifecycleOwner o,
                                                                               @NonNull String key,
                                                                               @NonNull Class<R> type1,
                                                                               @NonNull Class<S> type2,
                                                                               @NonNull Class<T> type3) {
        return new BusEventObserver<>(o, get().getLiveData(key, true, type1, type2, type3), key);
    }

    public static <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (get().classBusLiveDataMap) {
            BusLiveData<?> liveData = get().classBusLiveDataMap.remove(eventType);
            if (liveData == null) {
                return null;
            }
            return eventType.cast(liveData.getValue());
        }
    }

    public static Object removeStickyEvent(String key) {
        synchronized (get().keyBusLiveDataMap) {
            BusLiveData<?> liveData = get().keyBusLiveDataMap.remove(key);
            if (liveData == null || !liveData.isStickyEvent()) {
                return null;
            }
            return liveData.getValue();
        }
    }

    public static <T> T removeStickyEvent(String key, Class<T> type) {
        synchronized (get().multiBusLiveDataMap) {
            MultiKey multiKey = new MultiKey(key, type);
            BusLiveData<MultiEvent> liveData = get().multiBusLiveDataMap.remove(multiKey);
            if (liveData == null || !liveData.isStickyEvent() || liveData.getValue().getObjects().length != 1) {
                return null;
            }
            return type.cast(liveData.getValue().getObjectAt(0));
        }
    }

    public static void removeAllStickyEvents() {
        synchronized (get()) {
            for (Map.Entry<Class<?>, BusLiveData<?>> entry : get().classBusLiveDataMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isStickyEvent()) {
                    get().classBusLiveDataMap.remove(entry.getKey());
                }
            }
            for (Map.Entry<String, BusLiveData<String>> entry : get().keyBusLiveDataMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isStickyEvent()) {
                    get().keyBusLiveDataMap.remove(entry.getKey());
                }
            }
            for (Map.Entry<MultiKey, BusLiveData<MultiEvent>> entry : get().multiBusLiveDataMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isStickyEvent()) {
                    get().multiBusLiveDataMap.remove(entry.getKey());
                }
            }
        }
    }

    public static <T> T getStickyEvent(Class<T> event) {
        synchronized (get().classBusLiveDataMap) {
            BusLiveData<?> liveData = get().classBusLiveDataMap.get(event);
            if (liveData == null || !liveData.isStickyEvent()) {
                return null;
            }
            return event.cast(liveData.getValue());
        }
    }

    public static Object getStickyEvent(String key) {
        synchronized (get().keyBusLiveDataMap) {
            BusLiveData<?> liveData = get().keyBusLiveDataMap.get(key);
            if (liveData == null || !liveData.isStickyEvent()) {
                return null;
            }
            return liveData.getValue();
        }
    }

    public static <T> T getStickyEvent(String key, Class<T> type) {
        synchronized (get().multiBusLiveDataMap) {
            MultiKey multiKey = new MultiKey(key, type);
            BusLiveData<MultiEvent> liveData = get().multiBusLiveDataMap.get(multiKey);
            if (liveData == null || !liveData.isStickyEvent() || liveData.getValue().getObjects().length != 1) {
                return null;
            }
            return type.cast(liveData.getValue().getObjectAt(0));
        }
    }


    //---------------------------------------------------------------其他-----------------------------------------------------------------

    public static void removeObservers(Object o) {
        if (o == null) {
            return;
        }
        if (o instanceof String) {
            BusLiveData<String> liveData = get().keyBusLiveDataMap.remove(o);
            if (liveData != null) {
                liveData.removeObservers();
            }
        }
        for (BusLiveData<?> liveData : get().keyBusLiveDataMap.values()) {
            if (liveData != null) {
                liveData.removeObservers(o);
            }
        }

        for (BusLiveData<?> liveData : get().classBusLiveDataMap.values()) {
            if (liveData != null) {
                liveData.removeObservers(o);
            }
        }

        for (BusLiveData<?> liveData : get().multiBusLiveDataMap.values()) {
            if (liveData != null) {
                liveData.removeObservers(o);
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

    static final class BusLiveData<T> extends EventLiveData<T> {


        public BusLiveData(boolean isStickyEvent) {
            super(isStickyEvent);
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observe(owner, observer);
        }

        @Override
        public void removeObserver(@NonNull Observer<T> observer) {
            super.removeObserver(observer);
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


}
