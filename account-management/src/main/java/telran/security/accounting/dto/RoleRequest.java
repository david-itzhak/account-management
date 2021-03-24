package telran.security.accounting.dto;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class RoleRequest {

	@NotEmpty
	String userName;
	@NotEmpty
	String role;
}
