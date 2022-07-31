package com.daluga.future.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.daluga.future.Util.variableDelay;

@Service
public class RatingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingService.class);

    public String executeLongRunningService(String request) {
        LOGGER.debug("Thread started [" + request + "]");
        //forceNullPointer();
        return doWork(request);
    }

    private String doWork(String request) {
        variableDelay();
        //return "Request [" + request + "] " + createUUID();
        return "Request [" + request + "] " + Thread.currentThread().getName();
    }

}
