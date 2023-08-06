package jtlreporter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import jtlreporter.model.LogSamplesBody;
import jtlreporter.model.Sample;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JtlReporterListenerService {
    private static final Logger logger = LoggerFactory.getLogger(JtlReporterListenerService.class);
    private final String jwtToken;
    private final String itemId;
    private final String listenerUrl;
    private final List<Sample> sampleList;
    private final Gson gson;

    public JtlReporterListenerService(String jwtToken, String itemId, String listenerUrl) {
        this.jwtToken = jwtToken;
        this.itemId = itemId;
        this.sampleList = new LinkedList<Sample>();
        this.listenerUrl = listenerUrl;
        this.gson = new Gson();
    }

    /**
     * This method returns the current size of the ElasticSearch documents list
     *
     * @return integer representing the size of the ElasticSearch documents list
     */
    public int getListSize() {
        return this.sampleList.size();
    }

    /**
     * This method clears the ElasticSearch documents list
     */
    public void clearList() {
        this.sampleList.clear();
    }

    /**
     * This method adds a metric to the list (sampleList).
     *
     * @param sample Sample parameter
     */
    public void addToList(Sample sample) {
        this.sampleList.add(sample);
    }


    public void logSamples() {
        OkHttpClient client = new OkHttpClient();

        LogSamplesBody logSamplesBody = new LogSamplesBody(itemId, this.sampleList);
        String jsonString = gson.toJson(logSamplesBody, LogSamplesBody.class);
        logger.info("json body " + jsonString);

        RequestBody body = RequestBody.create(jsonString, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(listenerUrl + "/api/v2/test-run/log-samples")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-access-token", jwtToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 201) {
                logger.error("Unable to log samples");
                logger.error("Request failed: " + response.code() + " code " + response.body().string());
            }

        } catch (IOException e) {
            logger.error("Unable to start new test run " + e);
            throw new RuntimeException(e);
        }
    }

}
