package android.arch.lifecycle;

public interface EventObserver<T> extends Observer<T> {

    void onAttach();

    void onDetach();

    boolean hasTag(final Object tag);

}
