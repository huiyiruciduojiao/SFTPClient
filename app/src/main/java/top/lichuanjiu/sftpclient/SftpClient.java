package top.lichuanjiu.sftpclient;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class SftpClient extends Application {
    public static BlockingQueue<Runnable> taskQueue;
    /**
     * 数据库工具
     */
    public static SQLiteDatabase db;
    private ExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建线程池
        executor = Executors.newFixedThreadPool(1);
        // 创建任务队列
        taskQueue = new LinkedBlockingQueue<>();

        // 启动任务执行线程
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 获取任务并执行
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        // 停止任务执行线程
        executor.shutdown();
    }

    // 提供获取线程池和任务队列的方法
    public ExecutorService getExecutor() {
        return executor;
    }

    public BlockingQueue<Runnable> getTaskQueue() {
        return taskQueue;
    }

}

