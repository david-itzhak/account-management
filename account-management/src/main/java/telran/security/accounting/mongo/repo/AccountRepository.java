package telran.security.accounting.mongo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.security.accounting.mongo.documents.AccountDoc;

public interface AccountRepository extends UpdateMongoOperations, MongoRepository<AccountDoc, String> {

}
