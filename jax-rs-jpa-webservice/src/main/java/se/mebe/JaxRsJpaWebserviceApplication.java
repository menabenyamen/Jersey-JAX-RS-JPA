package se.mebe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(se.springdata.config.Config.class)
@ComponentScan(basePackages = {"se.springdata","se.mebe"})
public class JaxRsJpaWebserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaxRsJpaWebserviceApplication.class, args);
	}

}
