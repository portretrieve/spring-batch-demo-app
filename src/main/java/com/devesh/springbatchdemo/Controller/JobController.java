package com.devesh.springbatchdemo.Controller;

import com.devesh.springbatchdemo.Service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/importCustomers")
    public void importCsvToDBJob() {
        jobService.importCsvToDB();
    }
}