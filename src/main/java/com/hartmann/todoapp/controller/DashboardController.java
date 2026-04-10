package com.hartmann.todoapp.controller;

import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.service.TodoListService;
import com.hartmann.todoapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final TodoListService todoListService;
    private final UserService userService;

    public DashboardController(TodoListService todoListService, UserService userService) {
        this.todoListService = todoListService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername());
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("todoLists", todoListService.findAllByUser(user));
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @GetMapping("/new")
    public String newListForm() {
        return "lists/form";
    }

    @PostMapping("/new")
    public String createList(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Title cannot be empty.");
            return "redirect:/dashboard/new";
        }
        var created = todoListService.create(title.trim(), description, user);
        return "redirect:/lists/" + created.getId();
    }

    @GetMapping("/edit/{id}")
    public String editListForm(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("list", todoListService.findByIdAndUser(id, user));
        return "lists/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateList(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        todoListService.update(id, title.trim(), description, user);
        redirectAttributes.addFlashAttribute("success", "List updated successfully.");
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteList(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        todoListService.delete(id, user);
        redirectAttributes.addFlashAttribute("success", "List deleted.");
        return "redirect:/dashboard";
    }
}
