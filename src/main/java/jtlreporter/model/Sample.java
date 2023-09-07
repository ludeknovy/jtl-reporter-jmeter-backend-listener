package jtlreporter.model;

import java.net.URL;

public class Sample {
    private final Long timeStamp;
    private final Long elapsed;
    private final Long bytes;
    private final String label;
    private final String responseCode;
    private final String responseMessage;
    private final Boolean success;
    private final Integer grpThreads;
    private final Integer allThreads;
    private final Long latency;
    private final Long connect;
    private final String hostname;
    private final String threadName;
    private final String failureMessage;
    private final Long sentBytes;


    public Sample(
            Long timeStamp,
            Long elapsed,
            Long bytes,
            String label,
            String responseCode,
            String responseMessage,
            Boolean success,
            Integer grpThreads,
            Integer allThreads,
            Long latency,
            Long connect,
            URL hostname,
            String threadName,
            Long sentBytes,
            String failureMessage
    ) {
        this.timeStamp = timeStamp;
        this.elapsed = elapsed;
        this.bytes = bytes;
        this.label = label;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.success = success;
        this.grpThreads = grpThreads;
        this.allThreads = allThreads;
        this.latency = latency;
        this.connect = connect;
        this.hostname = hostname != null ? hostname.toString() : null;
        this.threadName = threadName;
        this.sentBytes = sentBytes;
        this.failureMessage = failureMessage;
    }
}
