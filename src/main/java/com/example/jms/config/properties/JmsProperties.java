package com.example.jms.config.properties;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jms")
public class JmsProperties {

  private Map<String, UseCase> config;

  @Data
  public static class UseCase {
    private ConnectionFactory connectionFactory;
  }

  @Data
  public static class ConnectionFactory {
    private String brokerURL;
    private String userName;
    private String password;
  }

}
