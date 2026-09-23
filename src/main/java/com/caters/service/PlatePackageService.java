package com.caters.service;

import com.caters.dto.PlatePackageDto;
import com.caters.entity.Caterers;
import com.caters.entity.PlatePackages;
import java.util.List;

public interface PlatePackageService {

    List<PlatePackages> getPackagesByCaterer(Caterers caterer);

    List<PlatePackages> getActivePackagesByCaterer(Caterers caterer);

    PlatePackages getPackageByIdWithDishes(Long packageId);

    PlatePackages createPackage(Caterers caterer, PlatePackageDto dto);

    PlatePackages updatePackage(Long packageId, Caterers caterer, PlatePackageDto dto);

    void togglePackageStatus(Long packageId, Caterers caterer);

    void deletePackage(Long packageId, Caterers caterer);
}