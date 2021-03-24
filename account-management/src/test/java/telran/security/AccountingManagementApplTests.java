package telran.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import lombok.extern.log4j.Log4j2;
import telran.security.accounting.AccountingManagementAppl;
import telran.security.accounting.dto.AccountRequest;
import telran.security.accounting.dto.AccountResponse;
import telran.security.accounting.mongo.repo.AccountRepository;
import telran.security.accounting.service.AccountingManagement;

@SpringBootTest(classes = AccountingManagementAppl.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Log4j2
class AccountingManagementApplTests {
	
	@Autowired
	AccountRepository repository;
	
	@Autowired
	AccountingManagement accountingManagement;
	
	@Autowired
	WebTestClient webTestClient;

	long nowTimeStamp = Instant.now().getEpochSecond();
	String[] rolesAdmin = {"admin"};
	String[] rolesUser = {"user"};
	String[] rolesAdminUser = {"admin", "user"};
	AccountRequest accountRequest = new AccountRequest ("user1", "00000000", rolesAdmin, 30);
	AccountResponse accountResponse = new AccountResponse ("user1", "********", rolesAdmin, Instant.now().getEpochSecond() + 30 * 60 * 60 *24);
	
	List<AccountRequest> accountRequestList = new ArrayList<>(Arrays.asList(
			new AccountRequest ("user1", "00000001", rolesAdmin, 30),
			new AccountRequest ("user2", "00000002", rolesUser, 30),
			new AccountRequest ("user3", "00000003", rolesUser, 30),
			new AccountRequest ("user4", "00000004", rolesUser, 30),
			new AccountRequest ("user5", "00000005", rolesAdminUser, 30)));
	
	private AccountResponse accountResponsefromAccountRequest (AccountRequest accountRequest) {
		return new AccountResponse (accountRequest.getUserName(), accountRequest.getPassword(), accountRequest.getRoles(), accountRequest.getExpiredPeriod());
	}
	
	@BeforeEach
	void cleanDb() {
		repository.deleteAll();
		log.debug(">>>> AccountingManagementApplTests > cleanDb : DB was cleaned");
	}
	
	@Test
	void contextLoads() {
		assertThat(repository).isNotNull();
		assertThat(accountingManagement).isNotNull();
		assertThat(webTestClient).isNotNull();
	}
	
	@Nested
	@DisplayName("Test for adding account")
	class AddingAccount{
		@Test
		@DisplayName("Test the method in the class AccountingManagementImpl")
		void saveAndReadAccount() {
			accountingManagement.addAccount(accountRequest);
			accountingManagement.getAccount("user1");
		}
		
		@Test
		@DisplayName("Test an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController")
		void addAccount() {
			log.debug(">>>> AccountingManagementApplTests > addAccount : start test");
			webTestClient.post()
					.uri("/accounts")
					.bodyValue(accountRequest)
					.exchange()
					.expectStatus().isOk()
					.expectBody(AccountResponse.class)
					.isEqualTo(accountResponse);
		}
	}
	
	@Test
	void deleteAccount() {
		accountingManagement.addAccount(accountRequest);
		accountingManagement.deleteAccount("user1");
		assertEquals(0, repository.count());
	}
	
	@Test
	void getAccount() {
		accountingManagement.addAccount(accountRequest);
		accountingManagement.getAccount("user1");
		assertEquals(accountResponse, accountingManagement.getAccount("user1"));
	}

	@Test
	void updatePassword() {
		log.debug(">>>> AccountingManagementApplTests > updatePassword : start test");
		accountingManagement.addAccount(accountRequest);
		log.debug(">>>> AccountingManagementApplTests > updatePassword : try to update password");
		accountingManagement.updatePassword("user1", "11111111");
		assertEquals("********", accountingManagement.getAccount("user1").getPassword());
	}

	@Test
	void updateSamePassword() {
		log.debug(">>>> AccountingManagementApplTests > updatePassword : start test");
		accountingManagement.addAccount(accountRequest);
		log.debug(">>>> AccountingManagementApplTests > updatePassword : try to update password");
		accountingManagement.updatePassword("user1", "11111111");
		assertEquals("********", accountingManagement.getAccount("user1").getPassword());
	}

	@Test
	void addRole() {
		accountingManagement.addAccount(accountRequest);
		accountingManagement.addRole("user1", "user");
		assertEquals("user", accountingManagement.getAccount("user1").getRoles()[1]);
	}

	@Test
	void removeRole() {
		accountingManagement.addAccount(accountRequest);
		accountingManagement.addRole("user1", "user");
		accountingManagement.removeRole("user1", "admin");
		assertEquals(1, accountingManagement.getAccount("user1").getRoles().length);
		assertEquals("user", accountingManagement.getAccount("user1").getRoles()[0]);
	}

	@Test
	void queryGetAccount() {
		accountRequestList.forEach(a -> accountingManagement.addAccount(a));
		webTestClient.get()
		.uri("/accounts/user1").exchange()
		.expectStatus().isOk()
		.expectBody(AccountResponse.class)
		.isEqualTo(accountResponsefromAccountRequest(new AccountRequest ("user1", "********", rolesAdmin, nowTimeStamp + 30 * 60 * 60 * 24)));
	}
}
