package com.motion.subscriber;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GreetingSubscriber {

  private static final Logger LOG = Logger.getLogger(GreetingSubscriber.class);

//  @ConfigProperty(name = "quarkus.google.cloud.project-id")
  String projectId = "mit-sb-cloudservices-4588";// Inject the projectId property from application.properties

  private TopicName topicName;
  private Subscriber subscriber;

  @Inject
  CredentialsProvider credentialsProvider;

  void startup(@Observes StartupEvent event) throws IOException {
    // Init topic and subscription, the topic must have been created before
    topicName = TopicName.of(projectId, "saka-topic-test");
    ProjectSubscriptionName subscriptionName = initSubscription();

    // Subscribe to PubSub
    MessageReceiver receiver = (message, consumer) -> {
      LOG.infov("Got message {0}", message.getData().toStringUtf8());
      consumer.ack();
    };
    subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
    subscriber.startAsync().awaitRunning();
  }

  @PreDestroy
  void destroy() {
    // Stop the subscription at destroy time
    if (subscriber != null) {
      subscriber.stopAsync();
    }
  }

  private ProjectSubscriptionName initSubscription() throws IOException {
    // List all existing subscriptions and create the 'test-subscription' if needed
    ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, "saka-test-subscription");
    SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
        .setCredentialsProvider(credentialsProvider)
        .build();
    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
      Iterable<Subscription> subscriptions = subscriptionAdminClient.listSubscriptions(ProjectName.of(projectId))
          .iterateAll();
      Optional<Subscription> existing = StreamSupport.stream(subscriptions.spliterator(), false)
          .filter(sub -> sub.getName().equals(subscriptionName.toString()))
          .findFirst();
      if (existing.isEmpty()) {
        subscriptionAdminClient.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance(), 0);
      }
    }
    return subscriptionName;
  }
}
