package com.motion.resource;

import static io.quarkus.arc.ComponentsProvider.LOG;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.motion.TestLiqp;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import liqp.Template;

@Path("/hello")
public class GreetingResource {

    @Inject
    private TestLiqp testLiqp;

    String projectId = "mit-sb-cloudservices-4588";//"fabiojapa";// Inject the projectId property from application.properties
    //"mit-sb-cloudservices-4588"

    private TopicName topicName;

    private Map<String, Integer> countMap = new HashMap<>();

    @Inject
    CredentialsProvider credentialsProvider;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello(JsonNode jsonNode) throws InterruptedException, IOException {
        if (jsonNode.has("message") && jsonNode.get("message").has("data")) {
            String code = jsonNode.get("message").get("data").asText();
            byte[] bytes = Base64.getDecoder().decode(code);
            code = new String(bytes, StandardCharsets.UTF_8);
            if (countMap.get(code) == null) {
                countMap.put(code, 0);
            }
            countMap.put(code, countMap.get(code) + 1);

            LOG.infov("json: " + code);
            LOG.infov("count: " + countMap.get(code));
            return Response.status(Integer.parseInt(code)).build();
        }
        File file = testLiqp.getFile("liquid/test.liquid");
        testLiqp.getFile("liquid/test.liquid");

        //Read file
        String lines = Files.readString(file.toPath());

        Template template = Template.parse(lines);
        String rendered = template.render(jsonNode.toString());
        System.out.println(rendered);


        topicName = TopicName.of(projectId, "saka-topic-test");
        Publisher publisher = Publisher.newBuilder(topicName)
            .setCredentialsProvider(credentialsProvider)
            .build();
        try {
            ByteString data = ByteString.copyFromUtf8(rendered); // Create a new message
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);// Publish the message
            ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {// Wait for message submission and log the result
                public void onSuccess(String messageId) {
                    LOG.infov("published with message id {0}", messageId);
                }

                public void onFailure(Throwable t) {
                    LOG.warnv("failed to publish: {0}", t);
                }
            }, MoreExecutors.directExecutor());
        } finally {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }

        return Response.ok(rendered).build();
    }

    @POST
    @Path("/http")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloHttp(JsonNode jsonNode) throws InterruptedException, IOException {
        if (jsonNode.has("message") && jsonNode.get("message").has("data")) {
            String code = jsonNode.get("message").get("data").asText();
            byte[] bytes = Base64.getDecoder().decode(code);
            code = new String(bytes, StandardCharsets.UTF_8);
            if (countMap.get(code) == null) {
                countMap.put(code, 0);
            }
            countMap.put(code, countMap.get(code) + 1);

            LOG.infov("json: " + code);
            LOG.infov("count: " + countMap.get(code));
            return Response.status(Integer.parseInt(code)).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/file")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloFile(JsonNode jsonNode) throws InterruptedException, IOException {
        System.out.println("jsonNode = " + jsonNode.toString());

        File file = testLiqp.getFile("liquid/test.liquid");
        testLiqp.getFile("liquid/test.liquid");
        testLiqp.getFile("liquid/test.liquid");

        //Read file
        String lines = Files.readString(file.toPath());

        Template template = Template.parse(lines);
        String rendered = template.render(jsonNode.toString());
        System.out.println(rendered);
        return rendered;
    }
}
