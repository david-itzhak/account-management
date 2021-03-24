package telran.security.accounting;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
//@PropertySource(value = "classpath:app.properties")
@Log4j2
public class AccountingManagementAppl {

	@Value("${test.message}")
	String test_app_prop;
	
	public static void main(String[] args) {
		SpringApplication.run(AccountingManagementAppl.class, args);
		
	}
	
    @PostConstruct
    public void postConstruct() {
    	log.debug(">>>> test app.propities: {}", test_app_prop);
    }
}
