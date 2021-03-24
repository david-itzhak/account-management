package telran.security.accounting.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class UpdatePasswordRequest {

	@NotEmpty
	String userName;
	
	@Min(8)
	String password;
}
