package com.newproject.etf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProposalController {

    @GetMapping("/would/you/marry/me")
    public String proposal() {
        return "proposal";
    }
}
