package com.mindlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindlog.entity.DailyReviewDetails;

public interface DailyReviewDetailsRepository extends JpaRepository<DailyReviewDetails, Long> {
}
