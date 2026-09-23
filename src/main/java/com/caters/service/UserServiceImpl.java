package com.caters.service;

import com.caters.dto.CatererRegisterDto;
import com.caters.dto.CustomerRegisterDto;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.entity.Users;
import com.caters.enums.Role;
import com.caters.repository.CaterersRepository;
import com.caters.repository.CustomerRepository;
import com.caters.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final CustomerRepository customersRepository;
    private final CaterersRepository caterersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UsersRepository usersRepository,
                           CustomerRepository customersRepository,
                           CaterersRepository caterersRepository,
                           PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.customersRepository = customersRepository;
        this.caterersRepository = caterersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isEmailAlreadyRegistered(String email) {
        return usersRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void registerCustomer(CustomerRegisterDto dto) {
        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        // 1. Create and save base Users entity using its parameterized constructor
        Users user = new Users(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                Role.ROLE_CUSTOMER
        );
        Users savedUser = usersRepository.save(user);

        // 2. Create and save Customers entity linking the saved Users entity
        Customers customer = new Customers(
                savedUser,
                dto.getName(),
                dto.getPhone(),
                dto.getAddress()
        );
        customersRepository.save(customer);
    }

    @Override
    @Transactional
    public void registerCaterer(CatererRegisterDto dto) {
        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        // 1. Create and save base Users entity with ROLE_CATERER
        Users user = new Users(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                Role.ROLE_CATERER
        );
        Users savedUser = usersRepository.save(user);

        // 2. Create and save Caterers entity (isVerified defaults to false)
        Caterers caterer = new Caterers(
                savedUser,
                dto.getBusinessName(),
                dto.getPhone(),
                dto.getAddress()
        );
        caterersRepository.save(caterer);
    }

    @Override
    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
}