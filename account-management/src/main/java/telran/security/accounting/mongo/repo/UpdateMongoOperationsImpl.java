package telran.security.accounting.mongo.repo;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import lombok.extern.log4j.Log4j2;
import telran.security.accounting.dto.AccountResponse;
import telran.security.accounting.mongo.documents.AccountDoc;

@Log4j2
public class UpdateMongoOperationsImpl implements UpdateMongoOperations {

	@Autowired
	MongoTemplate mongoTemplate;
	
	private AccountResponse accountResponsefromAccountDoc(AccountDoc accountDoc) {
		return new AccountResponse(accountDoc.getUserName(), accountDoc.getPassword(), accountDoc.getRoles(),
				accountDoc.getExpirationTimestamp());
	}
	
	private AccountResponse executeUpdateOperation(String username, Query query, Update update) {
		AccountDoc res = mongoTemplate.findAndModify(query, update, AccountDoc.class);
		if (res == null) {
			throw new RuntimeException(String.format("Unposible to execute operations. The account with the username %s not found, or the account allredy contains role, which you trying to add, or ther isn't role which you trying to remove.", username));
			// TODO negative test
		}
		return accountResponsefromAccountDoc(res);
	}

	@Override
	public AccountResponse updatePassword(String username, String password, long oldActivationTimestamp, long oldExpirationTimestamp) {
		long newActivation = Instant.now().getEpochSecond();
		long newExpiration = oldExpirationTimestamp - oldActivationTimestamp + newActivation;
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(username));
		Update update = new Update();
		update.set("password", password);
		update.set("activationTimestamp", newActivation);
		update.set("expirationTimestamp", newExpiration);
		AccountDoc res = mongoTemplate.findAndModify(query, update, AccountDoc.class);
		log.debug(">>>> UpdateMongoOperationsImpl > updatePassword: res: {}", res);
		if (res == null) {
			throw new RuntimeException(String.format("Unposible to execute operations. The account with the username %s not found", username));
			// TODO negative test
		}
		return accountResponsefromAccountDoc(res);
	}

	@Override
	public AccountResponse addRole(String username, String role) {
		// TODO scenario if not user with given username
		// TODO scenario if such a role is not foreseen
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(username).and("roles").nin(role));
		Update update = new Update();
		update.push("roles", role);
		return executeUpdateOperation(username, query, update);
	}

	@Override
	public AccountResponse removeRole(String username, String role) {
		// TODO scenario if not user with given username
		// TODO scenario if such a role not exists
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(username).and("roles").is(role));
		Update update = new Update();
		update.pull("roles", role);
		return executeUpdateOperation(username, query, update);
	}
}
