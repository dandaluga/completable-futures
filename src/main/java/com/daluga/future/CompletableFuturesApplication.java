package com.daluga.future;

import com.daluga.future.service.RatingService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SpringBootApplication
public class CompletableFuturesApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletableFuturesApplication.class);

    @Autowired
    private RatingService ratingService;

    public static void main(String[] args) {
		SpringApplication.run(CompletableFuturesApplication.class, args);
	}

    @Override
    public void run(String... strings) throws Exception {

        LOGGER.debug("The CompletableFuturesApplication has started!");

        LOGGER.debug("Number of processors: " + Runtime.getRuntime().availableProcessors());

        //blockingGetExample();
        nonBlockingGetExample();

        LOGGER.debug("The CompletableFuturesApplication has ended!");
    }

    private void nonBlockingGetExample() throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        ExecutorService executor = Executors.newCachedThreadPool();

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            return ratingService.executeLongRunningService("yoyoma1");
        }, executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            return ratingService.executeLongRunningService("yoyoma2");
        }, executor);

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            return ratingService.executeLongRunningService("yoyoma3");
        }, executor);

        // Timeouts for a CompletableFuture is currently a gap. Java 9 will be releasing
        // both the orTimeout and completeOnTimeout. You can read more here:
        // http://www.esynergy-solutions.co.uk/blog/asynchronous-timeouts-completable-future-java-8-and-9
        CompletableFuture<String> timeoutAfter = timeoutAfter(Duration.ofMillis(60000));

        future1.acceptEither(timeoutAfter, s -> LOGGER.debug(s))
                .exceptionally(t -> {LOGGER.error("Error thrown 1!!!", t); return null;});

        future2.acceptEither(timeoutAfter, s -> LOGGER.debug(s))
                .exceptionally(t -> {LOGGER.error("Error thrown 2!!!", t); return null;});

        future3.acceptEither(timeoutAfter, s -> LOGGER.debug(s))
                .exceptionally(t -> {LOGGER.error("Error thrown 3!!!", t); return null;});

        executor.shutdown();
    }

    // TODO: Refactor once Java 9 is available.
    // Taken from: https://dzone.com/articles/asynchronous-timeouts
    public static <T> CompletableFuture<T> timeoutAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + duration);
            return promise.completeExceptionally(ex);
        }, duration.toMillis(), MILLISECONDS);
        return promise;
    }

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(
                    1,
                    new ThreadFactoryBuilder()
                            .setDaemon(true)
                            .setNameFormat("failAfter-%d")
                            .build());

    private void blockingGetExample() throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        ExecutorService executor = Executors.newCachedThreadPool();

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            return ratingService.executeLongRunningService("yoyoma1");
        }, executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            return ratingService.executeLongRunningService("yoyoma2");
        }, executor);

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            return ratingService.executeLongRunningService("yoyoma3");
        }, executor);


        LOGGER.debug("Received: " + future1.get(50000, MILLISECONDS));
        LOGGER.debug("Received: " + future2.get(50000, MILLISECONDS));
        LOGGER.debug("Received: " + future3.get(50000, MILLISECONDS));

        executor.shutdown();
    }

}
