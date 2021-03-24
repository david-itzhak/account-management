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

import reactor.core.publisher.Mono;

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
	public Mono<UserDetails> addUser(UserDetails user) {
		return Mono.just(user)
				.doOnNext((userDetails) -> {
					String key = getKey(user.getUsername());
					this.users.put(key, userDetails);
				});
	}
	public Mono<UserDetails> removeUser(UserDetails user) {
		return Mono.just(user)
				.doOnNext((userDetails) -> {
					String key = getKey(user.getUsername());
					this.users.remove(key, userDetails);
				});
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
