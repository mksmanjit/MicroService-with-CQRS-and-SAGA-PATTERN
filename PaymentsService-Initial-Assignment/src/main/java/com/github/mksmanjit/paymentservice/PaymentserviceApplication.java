package com.github.mksmanjit.paymentservice;

import org.springframework.boot.SpringApplication;
import com.github.mksmanjit.core.config.AxonConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ AxonConfig.class })
public class PaymentserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentserviceApplication.class, args);
	}

}
