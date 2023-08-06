package jtlreporter;

import jtlreporter.model.Sample;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JtlReporterMetric {
    private static final Logger logger = LoggerFactory.getLogger(JtlReporterMetric.class);
    private SampleResult sampleResult;

    public JtlReporterMetric(SampleResult sr) {
        this.sampleResult = sr;
    }

    /**
     * This method returns the current metric as a Sample for the provided sampleResult
     *
     * @return a Sample
     */
    public Sample getMetric() throws Exception {

        return new Sample(
                this.sampleResult.getTimeStamp(),
                this.sampleResult.getTime(),
                this.sampleResult.getBytesAsLong(),
                this.sampleResult.getSampleLabel(),
                this.sampleResult.getResponseCode(),
                this.sampleResult.getResponseMessage(),
                this.sampleResult.isSuccessful(),
                this.sampleResult.getGroupThreads(),
                this.sampleResult.getAllThreads(),
                this.sampleResult.getLatency(),
                this.sampleResult.getConnectTime(),
                this.sampleResult.getURL()
        );
    }
}
