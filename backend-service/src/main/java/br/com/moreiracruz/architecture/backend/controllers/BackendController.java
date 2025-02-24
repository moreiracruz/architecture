package br.com.moreiracruz.architecture.backend.controllers;

import br.com.moreiracruz.architecture.backend.services.BackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BackendController {

    @Autowired
    private BackendService backendService;

    @GetMapping("/data")
    public String callService() {
        return backendService.data();
    }

}
