package com.likelion.welldone.repository;

import com.likelion.welldone.entity.DutyPoints;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutyPointsRepository extends JpaRepository<DutyPoints, Integer> {
}