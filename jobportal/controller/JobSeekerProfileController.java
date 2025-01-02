package com.minorproject.jobportal.controller;

import com.minorproject.jobportal.entity.JobSeekerProfile;
import com.minorproject.jobportal.entity.Skills;
import com.minorproject.jobportal.entity.Users;
import com.minorproject.jobportal.repository.UsersRepository;
import com.minorproject.jobportal.services.JobSeekerProfileService;
import com.minorproject.jobportal.util.FileDownloadUtil;
import com.minorproject.jobportal.util.FileUploadUtil;
import org.apache.catalina.util.ResourceSet;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.thymeleaf.util.StringUtils.*;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {

    private JobSeekerProfileService jobSeekerProfileService;
    private UsersRepository usersRepository;


    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
    }
    @GetMapping("/")
    public String JobSeekerProfile(Model model){
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        List<Skills> skills = new ArrayList<>();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(()->new UsernameNotFoundException("User not found. "));

            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            if(seekerProfile.isPresent()){
                jobSeekerProfile=seekerProfile.get();
                if(jobSeekerProfile.getSkills().isEmpty()){
                    skills.add(new Skills());
                    jobSeekerProfile.setSkills(skills);
                }
            }
            model.addAttribute("skills",skills);
            model.addAttribute("profile",jobSeekerProfile);
        }

        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile, @RequestParam("image")MultipartFile image, @RequestParam("pdf")MultipartFile pdf,Model model){

        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof  AnonymousAuthenticationToken)){
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(()->new UsernameNotFoundException("User not found. "));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }

        List<Skills>skillsList=new ArrayList<>();
        model.addAttribute("profile",jobSeekerProfile);
        model.addAttribute("skills",skillsList);

        for(Skills skills : jobSeekerProfile.getSkills()){

            skills.setJobSeekerProfile(jobSeekerProfile);
        }
        String imageName="";
        String resumeName="";

        if(!Objects.equals(image.getOriginalFilename(), "")){
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);
            
        }
        if(!Objects.equals(pdf.getOriginalFilename(), "")){
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);

        }

        JobSeekerProfile seekerProfile=jobSeekerProfileService.addNew(jobSeekerProfile);

        try{
            String uploadDir = "photos/candidate/"+jobSeekerProfile.getUserAccountId();
            if(!Objects.equals(image.getOriginalFilename(), "")){
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }
            if(!Objects.equals(pdf.getOriginalFilename(), "")){
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        }
        catch (IOException ex){
            throw new RuntimeException(ex);

        }
        return "redirect:/dashboard/";

    }


    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id,Model model){

        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);
        model.addAttribute("profile",seekerProfile.get());
        return "job-seeker-profile";

    }
    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam(value = "fileName")String fileName,@RequestParam(value = "userID") String userId){

        FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
        Resource resourse = null;
        try{
            resourse = fileDownloadUtil.getFileAsResourse("photos/candidate/"+userId, fileName);
        }catch (IOException io){
            return ResponseEntity.badRequest().build();
        }
        if(resourse==null){
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }
        String contentType="application/octet-stream";
        String headerValue="attachment; filename=\""+resourse.getFilename()+"\"";

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, headerValue).body(resourse);

    }
}
