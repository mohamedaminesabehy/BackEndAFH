package com.afh.gescomp.controller.deploy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DeployController {

    @RequestMapping(value = { "/home", "/dashboard", "/stat", "/marche","/fournisseur","/typegarantie","/typepenalite","/article", "/decompte", "/marche/**","/decompte/**","/profile","/profile/**","/chat" }, method = RequestMethod.GET)
    public String index() {
        return "forward:/static/index.html";  // Redirection vers index.html pour la racine
    }
}
