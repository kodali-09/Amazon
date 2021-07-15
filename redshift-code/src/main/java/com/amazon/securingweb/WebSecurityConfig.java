package com.amazon.securingweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests().antMatchers("/vendor/*/*/*", "/img/*", "/css/*", "/login", "/home","/dbstatus").permitAll()
//				.antMatchers(HttpMethod.POST, "/saveSensorDataPOST/**").permitAll()
//				.antMatchers(HttpMethod.POST, "/saveSensorDataPOST/").permitAll()
//				.antMatchers(HttpMethod.POST, "/saveSensorDataPOST").permitAll()
//				.antMatchers(HttpMethod.POST, "/saveSensorDataPOST/*").permitAll()
//
//				.anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll().and().logout()
//				.permitAll();
//
//		http.csrf().disable(); // **// ADD THIS CODE TO DISABLE CSRF IN PROJECT.**

		// http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
		// .antMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "USER")
		// .antMatchers(HttpMethod.POST, "/routeA/**").hasAnyRole("ADMIN", "USER")
		// .antMatchers(HttpMethod.POST, "/routeB/**").hasRole("ADMIN")
		// .antMatchers(HttpMethod.DELETE,
		// "/routeB/**").hasRole("ADMIN").and().requestCache()
		// .requestCache(new NullRequestCache()).and().cors().and().csrf().disable();
	}

	// @Bean
	// @Override
	// public UserDetailsService userDetailsService() {
	// UserDetails user =
	// User.withDefaultPasswordEncoder().username("user").password("password").roles("USER")
	// .build();
	//
	// InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
	// manager.createUser(user);
	// manager.createUser(
	// User.withDefaultPasswordEncoder().username("user1").password("password1").roles("USER1").build());
	// return manager;
	// // return new InMemoryUserDetailsManager(user);
	// }

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER").and().withUser("user1")
//				.password("{noop}password1").roles("USER", "ADMIN");
	}

	// @SuppressWarnings("deprecation")
	// @Bean
	// public static NoOpPasswordEncoder passwordEncoder() {
	// return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
	// }
}
