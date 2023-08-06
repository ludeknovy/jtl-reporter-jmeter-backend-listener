package jtlreporter.model;

import java.net.URL;

public class Sample {
    private Long timeStamp;
    private Long elapsed;
    private Long bytes;
    private String label;
    private String responseCode;
    private String responseMessage;
    private Boolean success;
    private Integer grpThreads;
    private Integer allThreads;
    private Long latency;
    private Long connect;
    private String hostname;
    private String threadName;
    private String failureMessage;
    private Long sentBytes;


    public Sample(
            Long timeStamp,
            Long  elapsed,
            Long  bytes,
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
        this.hostname = hostname.toString();
        this.threadName = threadName;
        this.sentBytes = sentBytes;
        this.failureMessage = failureMessage;
    }
}
