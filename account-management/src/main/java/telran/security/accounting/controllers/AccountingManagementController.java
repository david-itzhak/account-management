package telran.security.accounting.controllers;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;
import telran.security.accounting.api.ApiConstants;
import telran.security.accounting.dto.AccountRequest;
import telran.security.accounting.dto.AccountResponse;
import telran.security.accounting.dto.RoleRequest;
import telran.security.accounting.dto.UpdatePasswordRequest;
import telran.security.accounting.service.AccountingManagement;

@RestController
@Validated
@Log4j2
public class AccountingManagementController {
	
	@Autowired
	AccountingManagement accountingManagement;

	@GetMapping(value = ApiConstants.URL_GET)
	AccountResponse getAccount(@PathVariable(ApiConstants.ID) @NotEmpty String userName) {
		return accountingManagement.getAccount(userName);
	}
	
	@GetMapping(value = ApiConstants.URL_GET_ACTIVATED)
	List<AccountResponse> getActivatedAccounts() {
		return accountingManagement.getActivatedAccounts();
	}
	
	@PostMapping(value = ApiConstants.URL_POST)
	AccountResponse addAccount(@RequestBody @Valid AccountRequest accountDto) {
		log.debug(">>>> AccountingManagementController > addAccount: resieved REST POST (addAccount) for accountDto: {}", accountDto);
		AccountResponse res = accountingManagement.addAccount(accountDto);
		log.debug(">>>> AccountingManagementController > addAccount: resieved AccountResponse: {}", res);
		return res;
	}
	
	@PutMapping(value = ApiConstants.URL_UPDATE_PASSWORD)
	AccountResponse updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
		return accountingManagement.updatePassword(updatePasswordRequest.getUserName(), updatePasswordRequest.getPassword());
	}

	@PutMapping(value = ApiConstants.URL_NEW_ROLE)
	AccountResponse addNewRole(@RequestBody @Valid RoleRequest roleRequest) {
		return accountingManagement.addRole(roleRequest.getUserName(), roleRequest.getRole());
	}
	
	@PutMapping(value = ApiConstants.URL_REMOV_ROLE)
	AccountResponse removingExistingRole(@RequestBody @Valid RoleRequest roleRequest) {
		return accountingManagement.removeRole(roleRequest.getUserName(), roleRequest.getRole());
	}
	
	@DeleteMapping(value = ApiConstants.URL_POST)
	String removingAccount(@RequestParam(name = ApiConstants.USERNAME_PARAM) @NotEmpty String userName) {
		accountingManagement.deleteAccount(userName);
		return userName + " was deleted";
	}
}
