package az.bhos.teknofest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TeknofestApplication {

	static void main(String[] args) {
		SpringApplication.run(TeknofestApplication.class, args);
	}
}
