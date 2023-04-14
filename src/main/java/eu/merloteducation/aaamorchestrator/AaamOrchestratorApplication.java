package eu.merloteducation.aaamorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
@RestController
public class AaamOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AaamOrchestratorApplication.class, args);
	}


}
