package com.zpj.bus;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Schedulers {

    public interface Scheduler {

        void execute(Runnable runnable);

    }

    private static final class MainHolder {
        private static final MainScheduler MAIN = new MainScheduler();
    }

    private static final class IOHolder {
        private static final Scheduler IO = new IOScheduler();
    }

    public static Handler getMainHandler() {
        return MainHolder.MAIN.getMainHandler();
    }

    public static Scheduler main() {
        return MainHolder.MAIN;
    }

    public static Scheduler io() {
        return IOHolder.IO;
    }

    private static final class MainScheduler implements Scheduler  {

        private final Object mLock = new Object();

        @Nullable
        private volatile Handler mMainHandler;


        @Override
        public void execute(Runnable runnable) {
            getMainHandler().post(runnable);
        }

        @NonNull
        private Handler getMainHandler() {
            if (mMainHandler == null) {
                synchronized (mLock) {
                    if (mMainHandler == null) {
                        mMainHandler = new Handler(Looper.getMainLooper());
                    }
                }
            }
            return mMainHandler;
        }

    }

    private static final class IOScheduler implements Scheduler  {

        private final Object mLock = new Object();

        @Nullable
        private ExecutorService mDiskIO ;

        @Override
        public void execute(Runnable runnable) {
            if (mDiskIO == null) {
                synchronized (mLock) {
                    if (mDiskIO == null) {
                        mDiskIO = Executors.newFixedThreadPool(2);
                    }
                }
            }
            mDiskIO.execute(runnable);
        }
    }

}
