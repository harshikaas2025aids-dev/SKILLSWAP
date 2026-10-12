package com.skillswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebPageController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("currentNav", "dashboard");
        return "dashboard";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Student Registration");
        model.addAttribute("currentNav", "register");
        return "register";
    }

    @GetMapping("/profile")
    public String profile(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Student Profile");
        model.addAttribute("currentNav", "profile");
        return "profile";
    }

    @GetMapping("/skills")
    public String skills(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Manage Skills");
        model.addAttribute("currentNav", "skills");
        return "skills";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Explore & Search Skills");
        model.addAttribute("currentNav", "search");
        return "search";
    }

    @GetMapping("/matches")
    public String matches(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Smart Skill Matches");
        model.addAttribute("currentNav", "matches");
        return "matches";
    }

    @GetMapping("/requests")
    public String requests(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Exchange Requests");
        model.addAttribute("currentNav", "requests");
        return "requests";
    }

    @GetMapping("/students")
    public String students(@RequestParam(required = false, defaultValue = "1") int studentId, Model model) {
        model.addAttribute("activeStudentId", studentId);
        model.addAttribute("pageTitle", "Student Directory");
        model.addAttribute("currentNav", "students");
        return "students";
    }
}
