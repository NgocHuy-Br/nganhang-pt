package com.vn.nganhang_pt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Chào mừng đến với Hệ thống Ngân hàng Phân tán");
        model.addAttribute("subtitle", "Đồ án CSDL Phân tán - PTIT");
        return "index";
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("greeting", "Hello World!");
        return "hello";
    }
}
