package edu.sjsu.cmpe172.salon.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class HomeController extends BaseController {
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward("/index.jsp", request, response);
    }
}
