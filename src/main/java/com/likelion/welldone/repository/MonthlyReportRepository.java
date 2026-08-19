package com.likelion.welldone.repository;

import com.likelion.welldone.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, String> {
}