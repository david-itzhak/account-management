package telran.security.accounting.mongo.repo;

import telran.security.accounting.dto.AccountResponse;

public interface UpdateMongoOperations {
	AccountResponse updatePassword(String username, String password, long oldActivationTimestamp, long oldExpirationTimestamp);
	AccountResponse addRole(String username, String role);
	AccountResponse removeRole(String username, String role);
}
