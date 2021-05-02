package ie.cct.cbwa.Splittr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("ie.cct*")
public class SplittrMain {

	public static void main(String[] args) {
		SpringApplication.run(SplittrMain.class, args);
	}
}
