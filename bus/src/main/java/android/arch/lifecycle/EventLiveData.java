package android.arch.lifecycle;

import android.annotation.SuppressLint;
import android.arch.core.internal.SafeIterableMap;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zpj.bus.Schedulers;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;
import static android.arch.lifecycle.Lifecycle.State.STARTED;

/**
 * LiveData of event.
 * @param <T> The type of data held by this instance
 * @author Z-P-J
 */
@SuppressLint("RestrictedApi")
public class EventLiveData<T> extends LiveData<T> {

    private static final int START_VERSION = -1;
    private static final Object NOT_SET = new Object();

    private final SafeIterableMap<Observer<T>, ObserverWrapper> mObservers =
            new SafeIterableMap<>();

    // how many observers are in active state
    private final AtomicInteger mActiveCount = new AtomicInteger(0);

    private final AtomicInteger mVersion = new AtomicInteger(START_VERSION);

    private final boolean mIsStickyEvent;

    private volatile Object mData = NOT_SET;

    public EventLiveData(boolean isStickyEvent) {
        this.mIsStickyEvent = isStickyEvent;
    }

    public boolean isStickyEvent() {
        return mIsStickyEvent;
    }

    @Override
    protected void onActive() {

    }

    @Override
    protected void onInactive() {
        if (!mIsStickyEvent) {
            mData = NOT_SET;
        }
    }

    @Deprecated
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {

    }

    @Deprecated
    @Override
    public void observeForever(@NonNull Observer<T> observer) {

    }

    public void observeForever(@NonNull EventObserver<T> observer) {
        synchronized (mObservers) {
            ObserverWrapper wrapper = new ObserverWrapper(observer);
            ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
            if (existing != null) {
                if (!mIsStickyEvent) {
                    existing.updateVersion(mVersion.get());
                }
                return;
            }
            if (!mIsStickyEvent) {
                wrapper.updateVersion(mVersion.get());
            }
            observer.onAttach();
            wrapper.activeStateChanged(true);
        }
    }

    /**
     * Removes the given observer from the observers list.
     *
     * @param observer The Observer to receive events.
     */
    public void removeObserver(@NonNull final Observer<T> observer) {
        synchronized (mObservers) {
            ObserverWrapper removed = mObservers.remove(observer);
            if (removed == null) {
                return;
            }
            removed.detachObserver();
            removed.activeStateChanged(false);
            if (observer instanceof EventObserver) {
                ((EventObserver<T>) observer).onDetach();
            }
        }
    }

    public void removeObservers() {
        synchronized (mObservers) {
            for (Map.Entry<Observer<T>, ObserverWrapper> entry : mObservers) {
                removeObserver(entry.getKey());
            }
        }
    }

    public void removeObservers(@NonNull final Object tag) {
        synchronized (mObservers) {
            for (Map.Entry<Observer<T>, ObserverWrapper> entry : mObservers) {
                Observer<T> key = entry.getKey();
                if (key instanceof EventObserver && ((EventObserver<T>) key).hasTag(tag)) {
                    removeObserver(key);
                }
            }
        }
    }

    /**
     * Removes all observers that are tied to the given {@link LifecycleOwner}.
     *
     * @param owner The {@code LifecycleOwner} scope for the observers to be removed.
     */
    public void removeObservers(@NonNull final LifecycleOwner owner) {
        synchronized (mObservers) {
            for (Map.Entry<Observer<T>, ObserverWrapper> entry : mObservers) {
                if (entry.getValue().isAttachedTo(owner)) {
                    removeObserver(entry.getKey());
                }
            }
        }
    }

    @Override
    public void postValue(T value) {
        setValue(value);
    }

    @Override
    public void setValue(T value) {
        synchronized (mObservers) {
            int version = mVersion.incrementAndGet();
            mData = value;
            for (Map.Entry<Observer<T>, ObserverWrapper> entry : mObservers) {
                entry.getValue().considerNotify(value, version);
            }
        }
    }

    /**
     * Returns the current value.
     * Note that calling this method on a background thread does not guarantee that the latest
     * value set will be received.
     *
     * @return the current value
     */
    @Nullable
    public T getValue() {
        Object data = mData;
        if (data != NOT_SET) {
            //noinspection unchecked
            return (T) data;
        }
        return null;
    }

    @Override
    int getVersion() {
        return mVersion.get();
    }

    /**
     * Returns true if this LiveData has observers.
     *
     * @return true if this LiveData has observers
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasObservers() {
        return mObservers.size() > 0;
    }

    /**
     * Returns true if this LiveData has active observers.
     *
     * @return true if this LiveData has active observers
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasActiveObservers() {
        return mActiveCount.get() > 0;
    }

    private class ObserverWrapper {
        private final EventObserver<T> mObserver;
        private volatile boolean mActive;
        private volatile int mLastVersion = START_VERSION;

        private ObserverWrapper(EventObserver<T> observer) {
            mObserver = observer;
        }

        private void updateVersion(int version) {
            synchronized (this) {
                mLastVersion = version;
            }
        }

        private boolean compareOrUpdateVersion(int version) {
            synchronized (this) {
                if (mLastVersion >= version) {
                    return false;
                }
                mLastVersion = version;
                return true;
            }
        }

        private boolean shouldBeActive() {
            synchronized (mObserver) {
                return mObserver.isActive();
            }
        }

        boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        private synchronized void detachObserver() {
        }

        private synchronized void activeStateChanged(boolean newActive) {
            if (newActive == mActive) {
                return;
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive;
            boolean wasInactive = EventLiveData.this.mActiveCount.get() == 0;
            EventLiveData.this.mActiveCount.addAndGet(mActive ? 1 : -1);
            if (wasInactive && mActive) {
                onActive();
                if (!mIsStickyEvent) {
                    return;
                }
            }
            if (EventLiveData.this.mActiveCount.get() == 0 && !mActive) {
                onInactive();
                return;
            }
            if (mActive) {
                considerNotify(mData, mVersion.get());
            }
        }


        private synchronized void considerNotify(Object data, int version) {
            if (!mActive) {
                return;
            }
            // Check latest state b4 dispatch. Maybe it changed state but we didn't get the event yet.
            //
            // we still first check observer.active to keep it as the entrance for events. So even if
            // the observer moved to an active state, if we've not received that event, we better not
            // notify for a more predictable notification order.
            if (!shouldBeActive()) {
                activeStateChanged(false);
                return;
            }
            if (compareOrUpdateVersion(version)) {
                // noinspection unchecked
                mObserver.onChanged((T) data);
            }
        }

    }

}
