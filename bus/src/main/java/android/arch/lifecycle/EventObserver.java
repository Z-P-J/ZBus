package android.arch.lifecycle;

/**
 * A simple callback that can receive from {@link EventLiveData}.
 * @param <T> The type of the parameter
 * @author Z-P-J
 */
public interface EventObserver<T> extends Observer<T> {

    /**
     * On attach the observer.
     */
    void onAttach();

    /**
     * On detach the observer.
     */
    void onDetach();

    /**
     * Call this when {@link EventLiveData#removeObservers(Object)}
     * @param tag A tag of observer.
     * @return The {@link EventObserver} whether binding the tag.
     */
    boolean hasTag(final Object tag);

}
