package ru.ra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LoaderLoop {

    private static final Logger logger = LoggerFactory.getLogger(LoaderLoop.class);

    private ScheduledExecutorService sched;

    @Autowired
    private Loader loader;

    @Value("${loader.loop.interval-minutes}")
    private int intervalMinutes;

    @Value("${loader.loop.initial-minutes}")
    private int initialMinutes;

    @PostConstruct
    public void init() {
        sched = Executors.newSingleThreadScheduledExecutor();
        sched.scheduleAtFixedRate(() -> {
            loader.iteration();
        }, initialMinutes, intervalMinutes, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        sched.shutdownNow();
        if (!sched.awaitTermination(1, TimeUnit.MINUTES)) {
            logger.error("Could not shut down executor in the given time");
        }
    }
}
