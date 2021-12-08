package com.zpj.bus;

import android.arch.lifecycle.Observer;

interface EventObserver<T> extends Observer<T> {

    void onAttach();

    void onDetach();

}
