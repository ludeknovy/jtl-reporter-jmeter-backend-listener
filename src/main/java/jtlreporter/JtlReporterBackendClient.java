    package jtlreporter;

import java.io.IOException;
import java.util.*;

import jtlreporter.model.JwtResponse;
import jtlreporter.model.StartAsyncResponse;
import okhttp3.*;
import org.apache.http.ssl.TrustStrategy;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public class JtlReporterBackendClient extends AbstractBackendListenerClient {

    private static final String BUILD_NUMBER = "BuildNumber";
    private static final String JTL_BACKEND_URL = "jtlreporter.backend.url";
    private static final String JTL_LISTENER_SERVICE_URL = "jtlreporter.listener.service.url";
    private static final String JTL_API_TOKEN = "jtlreporter.api.token";
    private static final String JTL_BATCH_SIZE = "jtlreporter.batch.size";
    private static final String JTL_PROJECT_NAME = "jtlreporter.project.name";
    private static final String JTL_SCENARIO_NAME = "jtlreporter.scenario.name";
    private static final String JTL_ENVIRONMENT = "jtlreporter.environment";
    private static final Logger logger = LoggerFactory.getLogger(JtlReporterBackendClient.class);
    private static final Map<String, String> DEFAULT_ARGS = new LinkedHashMap<>();
    static {
        DEFAULT_ARGS.put(JTL_PROJECT_NAME, null);
        DEFAULT_ARGS.put(JTL_SCENARIO_NAME, null);
        DEFAULT_ARGS.put(JTL_ENVIRONMENT, null);
        DEFAULT_ARGS.put(JTL_BACKEND_URL, null);
        DEFAULT_ARGS.put(JTL_LISTENER_SERVICE_URL, null);
        DEFAULT_ARGS.put(JTL_API_TOKEN, null);
        DEFAULT_ARGS.put(JTL_BATCH_SIZE, "500");
    }
    private JtlReporterListenerService sender;
    private Set<String> fields;
    private int bulkSize;
    private long timeoutMs;

    private String itemId;

    private static final TrustStrategy TRUST_ALL_STRATEGY = (chain, authType) -> true;

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        DEFAULT_ARGS.forEach(arguments::addArgument);
        return arguments;
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        logger.info("Setting up");
        try {
            this.fields = new HashSet<>();
            this.bulkSize = Integer.parseInt(context.getParameter(JTL_BATCH_SIZE));

            if (bulkSize > 500) {
                logger.error("Max batch size (batch.size) is 500. Terminating execution.");
                throw new RuntimeException();
            }

            this.timeoutMs = 30000;

            logger.info("About to request JWT token");
            String jwtToken = this.getJwtToken(context);

            StartAsyncResponse startAsyncResponse = this.startNewTestRun(context);
            logger.info("New test run successfully registered with id " + startAsyncResponse.itemId);
            this.itemId = startAsyncResponse.itemId;


            this.sender = new JtlReporterListenerService(jwtToken, this.itemId, context.getParameter(JTL_LISTENER_SERVICE_URL));


            super.setupTest(context);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect to the JtlReporter", e);
        }
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        logger.info("test teardown");
        if (this.sender.getListSize() > 0) {
            // this.sender.sendRequest();
        }
        this.stopTestRun(context, this.itemId);
        super.teardownTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> results, BackendListenerContext context) {
        for (SampleResult sr : results) {
            JtlReporterMetric metric = new JtlReporterMetric(sr);

            try {
                this.sender.addToList(metric.getMetric());
            } catch (Exception e) {
                logger.error(
                        "JtlReporter Listener was unable to add sampler to the list of samplers to send... More info in JMeter's console.");
                e.printStackTrace();
            }
        }

        if (this.sender.getListSize() >= this.bulkSize) {
            try {
                this.sender.logSamples();
            } catch (Exception e) {
                logger.error("Error occured while sending bulk request.", e);
            } finally {
                this.sender.clearList();
            }
        }
    }

    private String getJwtToken(BackendListenerContext context) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("token", context.getParameter(JTL_API_TOKEN))
                .build();

        String url = context.getParameter(JTL_BACKEND_URL) + "/api/auth/login-with-token";

        logger.info(url);

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200) {
                Gson gson = new Gson();
                assert response.body() != null;
                JwtResponse jwtResponse = gson.fromJson(response.body().string(), JwtResponse.class);
                String jwtToken = jwtResponse.jwtToken;
                if (jwtToken == null) {
                    logger.error("No JWT token was returned, aborting execution");
                    throw new RuntimeException();
                }
                return jwtToken;
            } else {
                logger.error("Request failed: " + response.code() + " code " + response.body().string());
                throw new RuntimeException("No JWT token, cannot continue");
            }

        } catch (IOException e) {
            logger.error("Unable to get JWT Token " + e);
            throw new RuntimeException(e);
        }
    }

    private StartAsyncResponse startNewTestRun(BackendListenerContext context) {
        OkHttpClient client = new OkHttpClient();

        String projectName = context.getParameter(JTL_PROJECT_NAME);
        String scenarioName = context.getParameter(JTL_SCENARIO_NAME);
        String environment = context.getParameter(JTL_ENVIRONMENT);
        String token = context.getParameter(JTL_API_TOKEN);
        String url = context.getParameter(JTL_BACKEND_URL) + "/api/projects/" + projectName + "/scenarios/" + scenarioName + "/items/start-async";

        RequestBody formBody = new FormBody.Builder()
                .add("environment", environment)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("x-access-token",  token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 201) {
                Gson gson = new Gson();
                StartAsyncResponse startAsyncResponse = gson.fromJson(response.body().string(), StartAsyncResponse.class);
                logger.info(startAsyncResponse.itemId);

                if (startAsyncResponse.itemId == null) {
                    logger.error("No itemId returned");
                    throw new RuntimeException();
                }
                return startAsyncResponse;
            } else {
                logger.error("Request failed: " + response.code() + " code " + response.body().string());
                throw new RuntimeException("New async test run has not started, cannot continue");
            }

        } catch (IOException e) {
            logger.error("Unable to start new test run " + e);
            throw new RuntimeException(e);
        }
    }

    private void stopTestRun(BackendListenerContext context, String itemId) {
        OkHttpClient client = new OkHttpClient();
        logger.info("TEST ID " + itemId);
        String projectName = context.getParameter(JTL_PROJECT_NAME);
        String scenarioName = context.getParameter(JTL_SCENARIO_NAME);
        String token = context.getParameter(JTL_API_TOKEN);
        String url = context.getParameter(JTL_BACKEND_URL) + "/api/projects/" + projectName + "/scenarios/" + scenarioName + "/items/" + itemId + "/stop-async";

        RequestBody formBody = new FormBody.Builder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("x-access-token",  token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                logger.error("Request failed: " + response.code() + " code " + response.body().string());
            } else {
               logger.info("Test run was successfully ended");
            }

        } catch (IOException e) {
            logger.error("Unable to stop test run " + e);
        }

    }
}
