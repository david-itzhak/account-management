package telran.security.accounting.mongo.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "accounts")
public class AccountDoc {
//	Username as a document ID (@Id)
	@Id
	String userName;
//	Password
	String password;
//	Roles
	String[] roles;
//	Activation timestamp in the seconds (number seconds from 1970-01-01)
	long activationTimestamp;
//	Expiration timestamp in the seconds (number seconds from 1970-01-01)
	long expirationTimestamp;
}
