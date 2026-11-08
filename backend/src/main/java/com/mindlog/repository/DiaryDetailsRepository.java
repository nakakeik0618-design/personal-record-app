package com.mindlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindlog.entity.DiaryDetails;

public interface DiaryDetailsRepository extends JpaRepository<DiaryDetails, Long> {
}
