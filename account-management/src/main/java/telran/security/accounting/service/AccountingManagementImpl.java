package telran.security.accounting.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import telran.security.accounting.MapReactiveUserDetailsServiceCustom;
import telran.security.accounting.dto.AccountRequest;
import telran.security.accounting.dto.AccountResponse;
import telran.security.accounting.mongo.documents.AccountDoc;
import telran.security.accounting.mongo.repo.AccountRepository;

@Service
@Log4j2
public class AccountingManagementImpl implements AccountingManagement {

	@Autowired
	AccountRepository repository;

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	MapReactiveUserDetailsServiceCustom detailsService;

	private AccountResponse accountResponsefromAccountDoc(AccountDoc accountDoc) {
		return new AccountResponse(accountDoc.getUserName(), accountDoc.getPassword(), accountDoc.getRoles(),
				accountDoc.getExpirationTimestamp());
	}

	private AccountDoc accountDocfromAccountRequest(AccountRequest accountDto) {
		long activationTimestamp = Instant.now().getEpochSecond();
		return new AccountDoc(accountDto.getUserName(), accountDto.getPassword(), accountDto.getRoles(),
				activationTimestamp, activationTimestamp + accountDto.getExpiredPeriod() * 86400);
	}

	private String encodePassword(String password) {
		log.debug(">>>> AccountingManagementImpl > encodePassword : try to encode password");
		String encodedPassword = passwordEncoder.encode(password);
		log.debug(">>>> AccountingManagementImpl > encodePassword : {}", encodedPassword);
		return encodedPassword;
	}

	@Override
	public AccountResponse addAccount(AccountRequest accountDto) {
		log.debug(">>>> AccountingManagementImpl > addAccount : start creatin an account for accountDto {}",
				accountDto);
		if (repository.existsById(accountDto.getUserName())) {
			log.debug(">>>> AccountingManagementImpl > addAccount : the account for accountDto {} allready exists",
					accountDto);
			throw new RuntimeException(
					String.format("Unposible to add account. The account with the username %s already exists",
							accountDto.getUserName()));
			// TODO negative test
		}
		AccountDoc res = repository.save(accountDocfromAccountRequest(accountDto));
		log.debug(
				">>>> AccountingManagementImpl > addAccount : the account for accountDto {} was added. New AccountDoc: {}",
				accountDto, res);
		res.setPassword("*".repeat(8));
		return accountResponsefromAccountDoc(res);
	}

	@Override
	public void deleteAccount(String username) {
		if (!repository.existsById(username)) {
			throw new RuntimeException(String
					.format("Unposible to delete account. The account with the username %s not exists", username));
			// TODO negative test
		}
		repository.deleteById(username);
	}

	@Override
	public AccountResponse getAccount(String username) {
		AccountDoc account = repository.findById(username).orElse(null);
		if (account == null || account.getExpirationTimestamp() < Instant.now().getEpochSecond()) {
			return null;
		}
		account.setPassword("*".repeat(8));
		return accountResponsefromAccountDoc(account);
	}

	@Override
	public AccountResponse updatePassword(String username, String password) {
		AccountDoc account = repository.findById(username).orElse(null);
		if (account == null) {
			throw new RuntimeException(String
					.format("Unposible to update password. The account with the username %s not exists", username));
			// TODO negative test
		}
		log.debug(">>>> AccountingManagementImpl > encodePassword : try to matches new and old passwords");
		if (passwordEncoder.matches(password, "{noop}" + account.getPassword())) {
			throw new RuntimeException(
					String.format("Unposible to update password. The new password can not be same as the old"));
			// TODO negative test
		}
		log.debug(">>>> AccountingManagementImpl > encodePassword : try to apdate password");
		AccountResponse res = repository.updatePassword(username, encodePassword(password),
				account.getActivationTimestamp(), account.getExpirationTimestamp());
		if (res == null) {
			throw new RuntimeException(String
					.format("Unposible to execute operations. The account with the username %s not found", username));
			// TODO negative test
		}
		return res;
	}

	@Override
	public AccountResponse addRole(String username, String role) {
		AccountResponse res = repository.addRole(username, role);
		if (res == null) {
			throw new RuntimeException(String
					.format("Unposible to execute operations. The account with the username %s not found", username));
			// TODO negative test
		}
		return res;
	}

	@Override
	public AccountResponse removeRole(String username, String role) {
		AccountResponse res = repository.removeRole(username, role);
		if (res == null) {
			throw new RuntimeException(String
					.format("Unposible to execute operations. The account with the username %s not found", username));
			// TODO negative test
		}
		return res;
	}
}
