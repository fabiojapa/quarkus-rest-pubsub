package com.motion;

import static io.quarkus.arc.ComponentsProvider.LOG;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import liqp.Template;

@Path("/hello")
public class GreetingResource {

    @Inject
    private TestLiqp testLiqp;

    String projectId = "fabiojapa";// Inject the projectId property from application.properties
    //"mit-sb-cloudservices-4588"

    private TopicName topicName;

    @Inject
    CredentialsProvider credentialsProvider;

    private Subscriber subscriber;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(JsonNode jsonNode) throws InterruptedException, IOException {
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

        return rendered;
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
