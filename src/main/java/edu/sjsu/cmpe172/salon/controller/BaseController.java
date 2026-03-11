package edu.sjsu.cmpe172.salon.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseController {
  protected void forward(String jsp, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp" + jsp).forward(req, res);
  }
}