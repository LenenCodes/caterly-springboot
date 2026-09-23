package com.caters;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.caters.entity.Users;
import com.caters.enums.Role;
import com.caters.repository.UsersRepository;

@SpringBootApplication
public class CatersMainSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatersMainSpringbootApplication.class, args);
	}
	@Bean
	CommandLineRunner createAdminUser(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
	    return args -> {
	        usersRepository.findByEmail("admin@caters.com").ifPresentOrElse(
	            admin -> {
	               
	                admin.setPassword(passwordEncoder.encode("ADMIN_PASSWORD"));
	                usersRepository.save(admin);
	            },
	            () -> {
	                Users admin = new Users();
	                admin.setEmail("admin@caters.com");
	                admin.setPassword(passwordEncoder.encode(System.getenv("ADMIN_PASSWORD")));
	               
	                admin.setRole(Role.ROLE_ADMIN); 
	                usersRepository.save(admin);
	            }
	        );
	    };
	}
}
