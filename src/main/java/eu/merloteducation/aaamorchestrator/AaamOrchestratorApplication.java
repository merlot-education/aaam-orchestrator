package eu.merloteducation.aaamorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AaamOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AaamOrchestratorApplication.class, args);
	}


}
