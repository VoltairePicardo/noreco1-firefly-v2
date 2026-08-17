package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepo extends JpaRepository<Vehicle, Integer> {
}
