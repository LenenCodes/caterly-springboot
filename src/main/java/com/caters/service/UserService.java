package com.caters.service;

import com.caters.dto.CatererRegisterDto;
import com.caters.dto.CustomerRegisterDto;
import com.caters.entity.Users;

import java.util.Optional;

public interface UserService {

    boolean isEmailAlreadyRegistered(String email);

    void registerCustomer(CustomerRegisterDto dto);

    void registerCaterer(CatererRegisterDto dto);

    Optional<Users> findByEmail(String email);
}