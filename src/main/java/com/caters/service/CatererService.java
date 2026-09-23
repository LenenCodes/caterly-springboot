package com.caters.service;

import com.caters.dto.CatererProfileDto;
import com.caters.entity.Caterers;
import com.caters.entity.Users;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CatererService {

    Caterers getCatererByUser(Users user);

    Caterers getCatererByEmail(String email);

    Optional<Caterers> getCatererById(Long id);

    List<Caterers> getAllVerifiedCaterers();

    List<Caterers> getAllPendingCaterers();

    void verifyCaterer(Long catererId);

    Caterers updateCatererProfile(Long catererId, CatererProfileDto dto) throws IOException;
}