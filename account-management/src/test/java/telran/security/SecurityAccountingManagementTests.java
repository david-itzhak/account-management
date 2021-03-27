package telran.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import lombok.extern.log4j.Log4j2;
import telran.security.accounting.AccountingManagementAppl;
import telran.security.accounting.mongo.documents.AccountDoc;
import telran.security.accounting.mongo.repo.AccountRepository;
import telran.security.accounting.service.AccountingManagement;

@SpringBootTest(classes = AccountingManagementAppl.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Log4j2
@TestPropertySource("classpath:security_test.properties")
public class SecurityAccountingManagementTests {
	
	@Autowired
	WebTestClient webTestClient;
	@Autowired
	AccountRepository repository;
	@Autowired
	AccountingManagement accountingManagement;
	
	long nowTimeStamp = Instant.now().getEpochSecond();
	long inPast = Instant.now().getEpochSecond()-10000;
	long inFuture = Instant.now().getEpochSecond()+10000;
	String[] rolesAdmin = {"admin"};
	String[] rolesUser = {"user"};
	String[] rolesAdminUser = {"admin", "user"};
	List<AccountDoc> accountDocList = new ArrayList<>(Arrays.asList(
			new AccountDoc ("user1", "00000001", rolesAdmin, inPast, 1616619691), // expired
			new AccountDoc ("user2", "00000002", rolesUser, inPast, Instant.now().getEpochSecond()-1), // expired
			new AccountDoc ("user3", "00000003", rolesUser, inPast, inFuture), // active
			new AccountDoc ("user4", "00000004", rolesUser, inPast, inFuture), // active
			new AccountDoc ("user5", "00000005", rolesAdminUser, inPast, inFuture))); // active
	
	@BeforeEach
	void cleanDb() {
		repository.deleteAll();
		repository.saveAll(accountDocList);
		log.debug(">>>> AccountingManagementApplTests > cleanDb : DB was cleaned");
	}
	
	@Test
	void contextLoads() {
		assertThat(repository).isNotNull();
		assertThat(accountingManagement).isNotNull();
		assertThat(webTestClient).isNotNull();
	}

	@WithMockUser
	@Test
	void getUserAuthenticate() {
		webTestClient.get()
		.uri("/accounts/user5").exchange()
		.expectStatus().isOk();
	}
	
	@Test
	void getUserNotAuthenticate() {
		webTestClient.get()
		.uri("/accounts/user5").exchange()
		.expectStatus().isUnauthorized();
	}

}
