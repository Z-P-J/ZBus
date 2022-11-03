package com.zpj.bus;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The container of schedulers
 * @author Z-P-J
 */
public class Schedulers {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * Schedule the event.
     */
    public interface Scheduler {

        /**
         * Execute the runnable.
         * @param runnable
         */
        void execute(Runnable runnable);

    }

    private static final class MainHolder {
        private static final MainScheduler MAIN = new MainScheduler();
    }

    private static final class IOHolder {
        private static final Scheduler IO = new IOScheduler();
    }

    public static Handler getMainHandler() {
        return MainHolder.MAIN.mMainHandler;
    }

    public static Scheduler main() {
        return MainHolder.MAIN;
    }

    public static Scheduler io() {
        return IOHolder.IO;
    }

    /**
     * A {@link Scheduler} of main thread.
     */
    private static final class MainScheduler implements Scheduler  {

        private final Handler mMainHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            mMainHandler.post(runnable);
        }

    }

    /**
     * A {@link Scheduler} of io thread.
     */
    private static final class IOScheduler implements Scheduler  {

        private static final int THREAD_COUNT = 2 * CPU_COUNT + 1;
        private static final ThreadPoolExecutor IO_EXECUTOR = new ThreadPoolExecutor(
                THREAD_COUNT, THREAD_COUNT,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "IOThreadPool");
                    }
                }
        );

        @Override
        public void execute(Runnable runnable) {
            IO_EXECUTOR.execute(runnable);
        }
    }

}
