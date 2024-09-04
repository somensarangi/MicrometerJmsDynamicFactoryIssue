package com.example.jms.config;

import com.example.jms.config.properties.JmsProperties;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@Configuration
@EnableConfigurationProperties(JmsProperties.class)
public class JmsConfiguration {

  @Bean
  public JmsDynamicConfig jmsConsumerConfig(ConfigurableEnvironment environment, ObservationRegistry observationRegistry) {
    return new JmsDynamicConfig(environment, observationRegistry);
  }
}
