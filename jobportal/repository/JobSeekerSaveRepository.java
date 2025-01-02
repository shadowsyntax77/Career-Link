package com.minorproject.jobportal.repository;

import com.minorproject.jobportal.entity.JobPostActivity;
import com.minorproject.jobportal.entity.JobSeekerProfile;
import com.minorproject.jobportal.entity.JobSeekerSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSeekerSaveRepository extends JpaRepository<JobSeekerSave,Integer> {

    List<JobSeekerSave> findByUserId(JobSeekerProfile userAccountId);
    List<JobSeekerSave> findByJob(JobPostActivity job);


}
