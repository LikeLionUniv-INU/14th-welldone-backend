package com.likelion.welldone.repository;

import com.likelion.welldone.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {
  List<Routine> findByScheduledDateOrderByScheduledTimeAsc(LocalDate date);
  List<Routine> findByScheduledDateBetween(LocalDate start, LocalDate end);
}