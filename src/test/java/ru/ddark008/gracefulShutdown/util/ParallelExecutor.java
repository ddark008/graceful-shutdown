package ru.ddark008.gracefulShutdown.util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelExecutor {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    List<Future> futureList = new ArrayList<>();

    public int submit(Callable callable){
        Future future = executorService.submit(callable);
        futureList.add(future);
        return futureList.size()-1;
    }
    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        executorService.awaitTermination(timeout, unit);
        futureList.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public Future get(int i){
        return futureList.get(i);
    }
}
