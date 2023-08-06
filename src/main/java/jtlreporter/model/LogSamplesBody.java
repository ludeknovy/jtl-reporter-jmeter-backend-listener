package jtlreporter.model;

import java.util.List;

public class LogSamplesBody {
    private String itemId;
    private List<Sample> samples;

    public LogSamplesBody(String itemId, List<Sample> samples) {
        this.itemId = itemId;
        this.samples = samples;
    }
}
