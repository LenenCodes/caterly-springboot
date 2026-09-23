package com.caters.service;

import com.caters.dto.DishDto;
import com.caters.entity.Caterers;
import com.caters.entity.Dishes;
import java.util.List;

public interface DishService {
    List<Dishes> getDishesByCaterer(Caterers caterer);
    Dishes addDish(Caterers caterer, DishDto dishDto);
    void toggleAvailability(Long dishId, Caterers caterer);
    void deleteDish(Long dishId, Caterers caterer);
}