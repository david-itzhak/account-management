package telran.security.accounting.service;

import telran.security.accounting.dto.AccountRequest;
import telran.security.accounting.dto.AccountResponse;

public interface AccountingManagement {
	AccountResponse addAccount(AccountRequest accountDto);
	void deleteAccount(String username);
	AccountResponse getAccount(String username);
	AccountResponse updatePassword(String username, String password);
	AccountResponse addRole(String username, String role);
	AccountResponse removeRole(String username, String role);
}
