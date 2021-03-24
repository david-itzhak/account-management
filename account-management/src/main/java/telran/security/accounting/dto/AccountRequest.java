package telran.security.accounting.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class AccountRequest {

//	Username (not empty string)
	@NotEmpty
	String userName;
	
//	Password  (string with minimal 8 symbols)
	@Pattern(regexp = ".{8,}")
	String password;
	
//	Roles (array of the strings, possible empty)
	@NotNull
	String[] roles;
	
//	Expired period in days (positive number)
	@Min(0)
	long expiredPeriod;
}
