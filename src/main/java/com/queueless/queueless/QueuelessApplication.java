package com.queueless.queueless;

import com.queueless.queueless.config.AppProperties;
import com.queueless.queueless.config.KafkaTopicsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, KafkaTopicsProperties.class})
public class QueuelessApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueuelessApplication.class, args);
	}

}
