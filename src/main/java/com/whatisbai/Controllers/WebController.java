package com.whatisbai.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.whatisbai.Entities.Plants;
import com.whatisbai.Repositories.PlantsRepository;


@Controller
public class WebController {

    @Autowired
    PlantsRepository plantsRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/plantfix")
    public String plantFix(Model model) {

        List<Plants> allPlantsData = plantsRepository.findAll();
        List<String> allPlantsName = new ArrayList<>();

        model.addAttribute("plants", allPlantsData);
        return "plantfix";
    }

    @GetMapping("/testthankyou")
    public String testThankYou() {
        return "thankyouform";
    }

    @GetMapping("/testerror")
    public String error() {
        return "errorform";
    }
}
