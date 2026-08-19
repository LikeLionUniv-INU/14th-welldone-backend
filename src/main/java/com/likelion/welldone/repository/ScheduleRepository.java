package com.likelion.welldone.repository;

import com.likelion.welldone.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, LocalDate> {
  List<Schedule> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
}