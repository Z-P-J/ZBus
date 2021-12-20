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
     * @param o The object which binds to this observer
     * @return The {@link EventObserver} whether binding the object.
     */
    boolean isBindTo(final Object o);

    /**
     * Whether the event observer is activated.
     * @return active
     */
    boolean isActive();

}
