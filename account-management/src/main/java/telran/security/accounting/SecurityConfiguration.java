package telran.security.accounting;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
	
	@Value("${test.mod}")
	boolean testMod;

	@Autowired
	AccountRepository repository;
	
	public ConcurrentMap<String, UserDetails> mapUserDetails = new ConcurrentHashMap<>();

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

	@Bean
	MapReactiveUserDetailsService getMapDetailse() {
		if (testMod) {
			log.debug(">>>> SecurityConfiguration: getMapDetailse: start in test mod");
			UserDetails user = new User("user0", "00000000", AuthorityUtils.createAuthorityList("ROLE_USER"));
			UserDetails admin = new User("admin1", "11111111", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
			UserDetails users[] = { user, admin };
			mapUserDetails = Arrays.stream(users).collect(Collectors.toConcurrentMap(UserDetails::getUsername, ud -> ud));
			return new MapReactiveUserDetailsService(mapUserDetails);
		}
		log.debug(">>>> SecurityConfiguration: getMapDetailse: start in default mod");
		log.debug(">>>> SecurityConfiguration > getMapDetailse: start creation of @Bean getMapDetailse");
		List<AccountDoc> list = repository.findByExpirationTimestampGreaterThan(Instant.now().getEpochSecond());
		log.debug(">>>> SecurityConfiguration > getMapDetailse: get list of AccountDoc from repo: {}", list);
		List<UserDetails> listUserDetails = list.stream()
				.map(account -> new User(account.getUserName(), account.getPassword(),
							AuthorityUtils.createAuthorityList(rolesMapper(account.getRoles()))))
				.collect(Collectors.toList());
		log.debug(">>>> SecurityConfiguration > getMapDetailse: get list of UserDetails from repo: {}", listUserDetails);
		mapUserDetails = listUserDetails.stream().collect(Collectors.toConcurrentMap(UserDetails::getUsername, ud -> ud));
		return new MapReactiveUserDetailsService(mapUserDetails);
	}
//	private ConcurrentHashMap<String, UserDetails> listToMap(List<UserDetails> list){
//		ConcurrentHashMap<String, UserDetails> map = new ConcurrentHashMap<>();
//		for (UserDetails user : list) {
//			map.put(user.getUsername().toLowerCase(), user);
//		}
//		return map;
//	}
//	@Bean
//	MapReactiveUserDetailsServiceCustom getMapDetailse() {
//		if (testMod) {
//			log.debug(">>>> SecurityConfiguration: getMapDetailse: start in test mod");
//			UserDetails user = new User("user0", "00000000", AuthorityUtils.createAuthorityList("ROLE_USER"));
//			UserDetails admin = new User("admin1", "11111111", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
//			UserDetails users[] = { user, admin };
//			return new MapReactiveUserDetailsServiceCustom(users);
//		}
//		log.debug(">>>> SecurityConfiguration: getMapDetailse: start in default mod");
//		log.debug(">>>> SecurityConfiguration > getMapDetailse: start creation of @Bean getMapDetailse");
//		List<AccountDoc> list = repository.findByExpirationTimestampGreaterThan(Instant.now().getEpochSecond());
//		log.debug(">>>> SecurityConfiguration > getMapDetailse: get list of AccountDoc from repo: {}", list);
//		List<UserDetails> listUserDetails = list.stream()
//				.map(account -> new User(account.getUserName(), account.getPassword(),
//							AuthorityUtils.createAuthorityList(rolesMapper(account.getRoles()))))
//				.collect(Collectors.toList());
//		log.debug(">>>> SecurityConfiguration > getMapDetailse: get list of UserDetails from repo: {}", listUserDetails);
//		return new MapReactiveUserDetailsServiceCustom(listUserDetails);
//	}

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
