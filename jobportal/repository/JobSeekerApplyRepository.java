package com.minorproject.jobportal.repository;

import com.minorproject.jobportal.entity.JobPostActivity;
import com.minorproject.jobportal.entity.JobSeekerApply;
import com.minorproject.jobportal.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSeekerApplyRepository extends JpaRepository<JobSeekerApply,Integer> {

    List<JobSeekerApply>findByUserId(JobSeekerProfile userId);
    List<JobSeekerApply>findByJob(JobPostActivity job);
}
