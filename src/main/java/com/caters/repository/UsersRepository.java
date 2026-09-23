package com.caters.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caters.entity.Users;
import com.caters.enums.Role;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

	List<Users> findByRole(Role role);
	
	
	

	// Check if email belongs to ANOTHER user during UPDATES
	boolean existsByEmailAndIdNot(String email, Long id);

	Optional<Users> findByEmail(String email);

	boolean existsByEmail(String email);

}
