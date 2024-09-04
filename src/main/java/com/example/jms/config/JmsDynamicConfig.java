package com.example.jms.config;

import com.example.jms.config.properties.JmsProperties;
import com.example.jms.config.properties.JmsProperties.UseCase;
import io.micrometer.observation.ObservationRegistry;
import jakarta.jms.ConnectionFactory;
import java.util.Map;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

public class JmsDynamicConfig implements BeanDefinitionRegistryPostProcessor {

  private final Environment environment;
  private final ObservationRegistry observationRegistry;

  public JmsDynamicConfig(Environment environment, ObservationRegistry observationRegistry) {
    this.environment = environment;
    this.observationRegistry = observationRegistry;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

    // Bind properties using Binder
    JmsProperties jmsProperties = Binder.get(environment)
        .bind("jms", Bindable.of(JmsProperties.class))
        .orElseThrow(() -> new RuntimeException("Failed to bind JMS properties"));

    Map<String, JmsProperties.UseCase> useCases = jmsProperties.getConfig();

    if (useCases == null || useCases.isEmpty()) {
      throw new RuntimeException("No use cases found in JMS properties");
    }

    for (Map.Entry<String, JmsProperties.UseCase> entry : useCases.entrySet()) {
      String useCaseName = entry.getKey();
      JmsProperties.UseCase useCase = entry.getValue();
      try {
        // Create and register DefaultJmsListenerContainerFactory bean
        GenericBeanDefinition factoryBeanDefinition = new GenericBeanDefinition();
        factoryBeanDefinition.setBeanClass(DefaultJmsListenerContainerFactory.class);
        DefaultJmsListenerContainerFactory factory = createJmsListenerContainerFactory(useCase);
        factoryBeanDefinition.setInstanceSupplier(() -> factory);
        registry.registerBeanDefinition(useCaseName, factoryBeanDefinition);

        // Create and register JmsTemplate bean
        GenericBeanDefinition templateBeanDefinition = new GenericBeanDefinition();
        templateBeanDefinition.setBeanClass(JmsTemplate.class);
        JmsTemplate jmsTemplate = createJmsTemplate(
            connectionFactory(useCase));
        templateBeanDefinition.setInstanceSupplier(() -> jmsTemplate);
        registry.registerBeanDefinition(useCaseName + "JmsTemplate",
            templateBeanDefinition);

      } catch (Exception e) {
        throw new RuntimeException("Failed to create JMS beans for use case: " + useCaseName, e);
      }
    }
  }

  private DefaultJmsListenerContainerFactory createJmsListenerContainerFactory(
      JmsProperties.UseCase useCase)
      throws Exception {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    ConnectionFactory connectionFactory = connectionFactory(useCase);
    factory.setConnectionFactory(connectionFactory);
    factory.setObservationRegistry(observationRegistry);
    return factory;
  }

  private JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory) {
    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(connectionFactory);
    jmsTemplate.setObservationRegistry(observationRegistry);
    return jmsTemplate;
  }

  private ConnectionFactory connectionFactory(
      UseCase useCase) throws Exception {
    return setupConnectionFactory(useCase);
  }

  private ActiveMQConnectionFactory setupConnectionFactory(UseCase useCase) {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
    connectionFactory.setBrokerURL(useCase.getConnectionFactory().getBrokerURL());
    connectionFactory.setUserName(useCase.getConnectionFactory().getUserName());
    connectionFactory.setPassword(useCase.getConnectionFactory().getPassword());
    connectionFactory.setUseAsyncSend(true);
    return connectionFactory;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    // No implementation needed
  }
}
