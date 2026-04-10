package com.hartmann.todoapp.controller;

import com.hartmann.todoapp.dto.RegistrationForm;
import com.hartmann.todoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password.");
        if (logout != null) model.addAttribute("message", "You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("form") RegistrationForm form,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (!form.passwordsMatch()) {
            result.rejectValue("confirmPassword", "error.form", "Passwords do not match.");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form.getUsername(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.reject("error.form", e.getMessage());
            return "auth/register";
        }
    }
}
