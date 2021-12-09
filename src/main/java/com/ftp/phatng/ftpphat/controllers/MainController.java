package com.ftp.phatng.ftpphat.controllers;

import com.ftp.phatng.ftpphat.services.FtpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ftp")
public class MainController {

    @Autowired
    FtpClientService ftpClientService;

    @GetMapping
    public ResponseEntity<?> index() {
        ftpClientService.getFiles();
        return ResponseEntity.ok("hello world");
    }
}
