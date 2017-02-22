package com.netcracker.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Max on 21.02.2017.
 */
@Controller
public class MainController {

    @GetMapping(value = "/*", produces = MediaType.TEXT_HTML_VALUE)
    public String goIndex() {
        return "static/index.html";
    }

}