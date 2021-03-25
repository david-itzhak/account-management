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
import telran.security.accounting.mongo.documents.AccountDoc;
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
		
		@Nested
		class Positive{
			
			@Test
			@DisplayName("Positive test of the method in the class AccountingManagementImpl")
			void saveAndReadAccount() {
				accountingManagement.addAccount(accountRequest);
				accountingManagement.getAccount("user1");
			}
			
			@Test
			@DisplayName("Positive test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController")
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

			@Test
			@DisplayName("Positive test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController")
			void addAccount_withEmptyRoles() {
				accountRequest.setRoles(new String[]{});
				accountResponse.setRoles(new String[]{});
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
		
		@Nested
		class Negative{
			
			@Test
			@DisplayName("Negative test of the method in the class AccountingManagementImpl (try to add a user with the same userName)")
			void AccountingManagementImpl_addAccount_WithSameUsername_ExpectedRuntimeException() {
				accountingManagement.addAccount(accountRequest);
				assertThrows(RuntimeException.class, () -> accountingManagement.addAccount(accountRequest));
			}

			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with the same userName)")
			void AccountingManagementController_addAccount_WithSameUsername_Expecedt500() {
				accountingManagement.addAccount(accountRequest);
				webTestClient.post()
						.uri("/accounts")
						.bodyValue(accountRequest)
						.exchange()
						.expectStatus().is5xxServerError();
			}

			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with not valid pass)")
			void AccountingManagementController_addAccount_WithNotValidPass_Expecedt400() {
				accountRequest.setPassword("1111111");
				webTestClient.post()
				.uri("/accounts")
				.bodyValue(accountRequest)
				.exchange()
				.expectStatus().isBadRequest();
			}
			
			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with empty name)")
			void AccountingManagementController_addAccount_WithEmptyName_Expecedt400() {
				accountRequest.setUserName("");
				webTestClient.post()
				.uri("/accounts")
				.bodyValue(accountRequest)
				.exchange()
				.expectStatus().isBadRequest();
			}

			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with null name)")
			void AccountingManagementController_addAccount_WithNullName_Expecedt400() {
				accountRequest.setUserName(null);
				webTestClient.post()
				.uri("/accounts")
				.bodyValue(accountRequest)
				.exchange()
				.expectStatus().isBadRequest();
			}

			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with null roles)")
			void AccountingManagementController_addAccount_WithNullRoles_Expecedt400() {
				accountRequest.setRoles(null);
				webTestClient.post()
				.uri("/accounts")
				.bodyValue(accountRequest)
				.exchange()
				.expectStatus().isBadRequest();
			}

			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with expiredPeriod 0)")
			void AccountingManagementController_addAccount_WithexpiredPeriod0_Expecedt400() {
				accountRequest.setExpiredPeriod(0);
				webTestClient.post()
				.uri("/accounts")
				.bodyValue(accountRequest)
				.exchange()
				.expectStatus().isBadRequest();
			}

			@Test
			@DisplayName("Negative test of an POST endpoint \"\\accounts\" (addAccount) in class the AccountingManagementController (try to add a user with expiredPeriod negative)")
			void AccountingManagementController_addAccount_WithexpiredPeriodNegative_Expecedt400() {
				accountRequest.setExpiredPeriod(-1);
				webTestClient.post()
				.uri("/accounts")
				.bodyValue(accountRequest)
				.exchange()
				.expectStatus().isBadRequest();
			}
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

	@Test
	void getActivatedAccounts() {
		long inPast = Instant.now().getEpochSecond()-10000;
		long inFuture = Instant.now().getEpochSecond()+10000;
		List<AccountDoc> accountDocList = new ArrayList<>(Arrays.asList(
				new AccountDoc ("user1", "00000001", rolesAdmin, inPast, 1616619691), // expired
				new AccountDoc ("user2", "00000002", rolesUser, inPast, Instant.now().getEpochSecond()-1), // expired
				new AccountDoc ("user3", "00000003", rolesUser, inPast, inFuture), // active
				new AccountDoc ("user4", "00000004", rolesUser, inPast, inFuture), // active
				new AccountDoc ("user5", "00000005", rolesAdminUser, inPast, inFuture))); // active
		List<AccountResponse> accountResponseList = new ArrayList<>(Arrays.asList(
				new AccountResponse ("user3", "********", rolesUser, 1716619690),
				new AccountResponse ("user4", "********", rolesUser, 1716619690),
				new AccountResponse ("user5", "********", rolesAdminUser, 1716619690)));
		repository.saveAll(accountDocList);
		webTestClient.get()
		.uri("/accounts/activated").exchange()
		.expectStatus().isOk()
		.expectBodyList(AccountResponse.class)
		.isEqualTo(accountResponseList);
	}
}
