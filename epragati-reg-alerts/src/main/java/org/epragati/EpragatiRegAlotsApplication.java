package org.epragati;

import java.util.Arrays;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
@SpringBootApplication
public class EpragatiRegAlotsApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(EpragatiRegAlotsApplication.class, args);
	}
	
	@Value("${broker.url}")
    private String brokerUrl;
	
	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
	    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
	    factory.setTrustedPackages(Arrays.asList("org.epragati.reg.notification","org.epragati.reg.notification.response"));
	    factory.setTrustAllPackages(true);
	    return factory;
	}
}
