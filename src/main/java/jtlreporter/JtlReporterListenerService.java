package jtlreporter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import jtlreporter.model.Constants;
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

    private final Integer bulkSize;
    private final List<Sample> sampleList;
    private final Gson gson;


    public JtlReporterListenerService(String jwtToken, String itemId, String listenerUrl, Integer bulkSize) {
        this.jwtToken = jwtToken;
        this.itemId = itemId;
        this.sampleList = new LinkedList<Sample>();
        this.listenerUrl = listenerUrl;
        this.bulkSize = bulkSize;
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


    public Boolean logSamples() {
        try {
            logger.info("sending samples to JtlReporter listener service");

            int listSize = this.getListSize();
            int lastElement = listSize >= bulkSize ? bulkSize : listSize;
            logger.info("last element " + lastElement);
            List<Sample> subList = this.sampleList.subList(0, lastElement);

            logger.info("sublist " + subList.size());
            LogSamplesBody logSamplesBody = new LogSamplesBody(itemId, subList);
            String jsonString = gson.toJson(logSamplesBody, LogSamplesBody.class);

            RequestBody body = RequestBody.create(jsonString, MediaType.parse("application/json"));

            this.logSamplesRequest(body);
            subList.clear();
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while sending bulk request.", e);
            return false;
        }

    }

    private void logSamplesRequest(RequestBody body) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(listenerUrl + "/api/v3/test-run/log-samples")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader(Constants.X_ACCESS_TOKEN, jwtToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 201) {
                logger.error("Request failed: " + response.code() + " code " + response.body().string());
                throw new Exception("Request failed: " + response.code() + " code " + response.body().string());
            }

        } catch (IOException e) {
            logger.error("Unable log samples " + e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
