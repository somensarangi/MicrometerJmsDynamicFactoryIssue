package com.example.jms.receiver;

import com.example.jms.config.properties.JmsProperties;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JmsReceiver {

  @JmsListener(destination = "test-queue1",
  containerFactory = "customerService")
  public void receivedJmsMessage(Message message) {
    try {
      log.info("Received Message : {}", message);

    } catch (Exception e) {
      log.error("Error while processing Jms message", e);
    }
  }
}
