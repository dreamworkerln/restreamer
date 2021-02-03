package ru.kvanttelecom.tv.restreamer.utils.threadpool;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.tascalate.concurrent.CompletableTask;
import net.tascalate.concurrent.Promise;
import net.tascalate.concurrent.Promises;
import net.tascalate.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class BlockingJobPool<A, R> {

    private final ThreadPoolExecutor threadPool;
    private final Duration timeout;

    // On each job done handler
    private final Consumer<JobResult<A, R>> callback;

    private final AtomicInteger activeThreads = new AtomicInteger(0);

    public BlockingJobPool(Integer poolSize, Duration timeout, Consumer<JobResult<A, R>> callback) {

        this.timeout = timeout;

        if (poolSize == null || poolSize <= 0) {
            poolSize = 1;
        }

        if (callback == null) {
            throw new IllegalArgumentException("ERROR: callback == null");
        }
        this.callback = callback;

        final CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();
        threadFactory.setDaemon(true);
        threadFactory.setThreadNamePrefix("BlockingJobPool-");



        threadPool = new ThreadPoolTaskExecutor(
            poolSize,poolSize,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            //new LinkedBlockingQueue<>(poolSize),
            threadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy());

    }

    /**
     *
     * @param argument - argument to identify task)
     * @param function - method to execute asynchronously with given identifier as argument

     */
    public void add(A argument, Function<A, JobResult<A,R>> function) {


        //System.out.println("callback: + " + Thread.currentThread().getName());
        CompletableTask
            .supplyAsync(() -> function.apply(argument), threadPool)
            // выставляем timeout, начинает отсчитываться при вызове supplyAsync(),
            // даже если задача попала в ThreadPoolExecutor.workQueue
            .orTimeout(timeout)
            // generate JobResult on exceptional execution
            .exceptionallyAsync(throwable -> {
                    JobResult<A,R> r = new JobResult<>(argument);
                    r.setException(throwable);
                    return r;
                }
            )
            // Оповещаем о завершении каждой задачи
            .thenAcceptAsync(
                callback
            )
            .exceptionallyAsync(throwable -> {
                    log.error("blockingJobPool.callback have been executed with error: ", throwable);
                    return null;
                }
            );
    }

    /**
     * Add batch jobs blocking
     * Will block calling thread till all promises have been completed
     @return List<JobResult>
     */
    public List<JobResult<A,R>> batch(List<BatchItem<A, R>> batchList) throws ExecutionException, InterruptedException {

        List<Promise<JobResult<A,R>>> promiseList = batchJobs(batchList);
        return Promises.all(promiseList).get();
    }

    /**
     * Add batch jobs async
     * <br>No wait, no results
     */
    public void batchAsync(List<BatchItem<A, R>> batchList) {
        batchJobs(batchList);
    }

    /**
     * Add batch jobs async
     * Will async call callback when all promises have been completed
     @return List<JobResult>
     */
    public void batchAsync(List<BatchItem<A, R>> batchList, Consumer<List<JobResult<A,R>>> callback) throws ExecutionException, InterruptedException {

        List<Promise<JobResult<A,R>>> promiseList = batchJobs(batchList);
        Promises.all(promiseList).thenAccept(callback);
    }

    public void shutdown() throws InterruptedException {
        System.out.println("Awaiting termination ...");
        threadPool.shutdown();
        threadPool.awaitTermination(10000, TimeUnit.MILLISECONDS);
    }




    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        threadPool.awaitTermination(timeout, unit);
    }

    public boolean isTerminated()  {
        return  threadPool.isTerminated();
    }

    public int getActiveCount() {
        return activeThreads.get();
    }



    // ========================================================================


    private List<Promise<JobResult<A,R>>> batchJobs(List<BatchItem<A, R>> batchList) {

        List<Promise<JobResult<A,R>>> result = new ArrayList<>();

        batchList.forEach(item -> {

            Promise<JobResult<A,R>> promise =
                CompletableTask
                    .supplyAsync(() -> item.getFunction().apply(item.getArgument()), threadPool)
                    .orTimeout(timeout)
                    .handle((t, throwable) -> {
                        if (throwable != null) {
                            t = new JobResult<>(item.getArgument());
                            t.setException(throwable);
                        }
                        return t;
                    });
            result.add(promise);
        });

        return result;
    }
}



/*
            .handleAsync((t, throwable) -> {
                if (throwable != null) {
                    t = new JobResult<>(identifier);
                    t.setException(throwable);
                }
                return t;
            })




 */