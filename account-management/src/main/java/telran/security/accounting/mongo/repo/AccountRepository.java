package telran.security.accounting.mongo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.security.accounting.mongo.documents.AccountDoc;

public interface AccountRepository extends UpdateMongoOperations, MongoRepository<AccountDoc, String> {
	List<AccountDoc> findByExpirationTimestampGreaterThan(long timeStampNow);
}
