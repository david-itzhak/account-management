package telran.security.accounting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class MapReactiveUserDetailsServiceCustom
		implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
	private Map<String, UserDetails> users;

	public MapReactiveUserDetailsServiceCustom(Map<String, UserDetails> users) {
		this.users = users;
	}
	public MapReactiveUserDetailsServiceCustom(UserDetails... users) {
		this(Arrays.asList(users));
	}
	public MapReactiveUserDetailsServiceCustom(Collection<UserDetails> users) {
		Assert.notEmpty(users, "users cannot be null or empty");
		this.users = new ConcurrentHashMap<>();
		for (UserDetails user : users) {
			this.users.put(getKey(user.getUsername()), user);
		}
		log.debug(">>>> MapReactiveUserDetailsServiceCustom > constractor: {}", users.toString());
	}
	@Override
	public Mono<UserDetails> findByUsername(String username) {
		String key = getKey(username);
		UserDetails result = this.users.get(key);
		return (result != null) ? Mono.just(User.withUserDetails(result).build()) : Mono.empty();
	}
	@Override
	public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
		return Mono.just(user)
				.map((userDetails) -> withNewPassword(userDetails, newPassword))
				.doOnNext((userDetails) -> {
					String key = getKey(user.getUsername());
					this.users.put(key, userDetails);
				});
	}
	public void addOrChangeUser(UserDetails user) {
		log.debug(">>>> MapReactiveUserDetailsServiceCustom > addUser: start adding a new user to map details service");
		users.put(getKey(user.getUsername()), user);
		log.debug(">>>> MapReactiveUserDetailsServiceCustom > addUser: map after addUser{}", users.toString());
	}
	public void removeUser(UserDetails user) {
		users.remove(getKey(user.getUsername()));
	}
	public void removeUser(String user) {
		users.remove(user);
	}
	public void addRole(UserDetails user) {
		log.debug(">>>> MapReactiveUserDetailsServiceCustom > addUser: start adding a new user to map details service");
		users.put(getKey(user.getUsername()), user);
		log.debug(">>>> MapReactiveUserDetailsServiceCustom > addUser: map after addUser{}", users.toString());
	}

	private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
		return User.withUserDetails(userDetails)
				.password(newPassword)
				.build();
	}
	private String getKey(String username) {
		return username.toLowerCase();
	}
}
