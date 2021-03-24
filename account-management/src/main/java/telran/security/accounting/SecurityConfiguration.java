package telran.security.accounting;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.extern.log4j.Log4j2;
import telran.security.accounting.api.ApiConstants;
import telran.security.accounting.mongo.documents.AccountDoc;
import telran.security.accounting.mongo.repo.AccountRepository;

@Configuration
@Log4j2
//@EnableWebSecurity(debug = true)
public class SecurityConfiguration {

	@Value("${security.enable}")
	boolean securityEnable;

	@Autowired
	AccountRepository repository;

	@Bean
	PasswordEncoder getPasswordEncoder() {
		log.debug(">>>> SecurityConfiguration: start creation of @Bean PasswordEncoder");
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	private String[] rolesMapper(String[] roles) {
		log.debug(">>>> SecurityConfiguration: mappin roles to format for creating User object: {}",
				Arrays.deepToString(roles));
		String[] rolesNew = Arrays.stream(roles).map(role -> String.format("ROLE_%s", role).toUpperCase())
				.toArray(String[]::new);
		log.debug(">>>> SecurityConfiguration: roles in format for User object: {}", Arrays.deepToString(rolesNew));
		return rolesNew;
	}

//	@Bean
//	MapReactiveUserDetailsService getMapDetailse() {
//		if (!securityEnable) {
//			log.debug(">>>> SecurityConfiguration: getMapDetailse: start in test mod");
//			UserDetails user = new User("user", "{noop}user", AuthorityUtils.createAuthorityList("ROLE_USER"));
//			UserDetails admin = new User("admin", "{noop}admin", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
//			UserDetails users[] = { user, admin };
//			return new MapReactiveUserDetailsService(users);
//		}
//		List<AccountDoc> list = repository.findAll();
//		log.debug(">>>> SecurityConfiguration > getMapDetailse: get list of AccountDoc from repo: {}", list);
//		List<UserDetails> listUserDetails = list.stream()
//				.filter(account -> account.getExpirationTimestamp() > Instant.now().getEpochSecond()).map(account -> {
//					return new User(account.getUserName(), String.format("{noop}%s", account.getPassword()),
//							AuthorityUtils.createAuthorityList(rolesMapper(account.getRoles())));
//				}).collect(Collectors.toList()); // TODO negative test for a case, when expiration timestamp of the
//													// password expired
//		return new MapReactiveUserDetailsService(listUserDetails);
//	}
	@Bean
	MapReactiveUserDetailsServiceCustom getMapDetailse() {
		if (!securityEnable) {
			log.debug(">>>> SecurityConfiguration: getMapDetailse: start in test mod");
			UserDetails user = new User("user", "{noop}user", AuthorityUtils.createAuthorityList("ROLE_USER"));
			UserDetails admin = new User("admin", "{noop}admin", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
			UserDetails users[] = { user, admin };
			return new MapReactiveUserDetailsServiceCustom(users);
		}
		List<AccountDoc> list = repository.findAll();
		log.debug(">>>> SecurityConfiguration > getMapDetailse: get list of AccountDoc from repo: {}", list);
		List<UserDetails> listUserDetails = list.stream()
				.filter(account -> account.getExpirationTimestamp() > Instant.now().getEpochSecond()).map(account -> {
					return new User(account.getUserName(), String.format("{noop}%s", account.getPassword()),
							AuthorityUtils.createAuthorityList(rolesMapper(account.getRoles())));
				}).collect(Collectors.toList()); // TODO negative test for a case, when expiration timestamp of the
		// password expired
		return new MapReactiveUserDetailsServiceCustom(listUserDetails);
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
		log.debug(">>>> SecurityConfiguration: flag securityEnable is: {}", securityEnable);
		if (!securityEnable) {
			SecurityWebFilterChain filterChain = httpSecurity.csrf().disable().authorizeExchange().anyExchange()
					.permitAll().and().build();
			log.debug(">>>> SecurityConfiguration: set security to disable");
			return filterChain;
		}
		SecurityWebFilterChain filterChain = httpSecurity.csrf().disable().httpBasic().and().authorizeExchange()
				.pathMatchers(HttpMethod.GET).hasRole(ApiConstants.USER).pathMatchers(HttpMethod.POST)
				.hasRole(ApiConstants.ADMIN).pathMatchers(HttpMethod.DELETE).hasRole(ApiConstants.ADMIN)
				.pathMatchers(HttpMethod.PUT).hasRole(ApiConstants.ADMIN).and().build();
		log.debug(">>>> SecurityConfiguration: set security to enable");
		return filterChain;
	}
}
