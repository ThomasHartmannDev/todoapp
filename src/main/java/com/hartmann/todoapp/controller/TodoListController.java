package com.hartmann.todoapp.controller;

import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.service.TodoItemService;
import com.hartmann.todoapp.service.TodoListService;
import com.hartmann.todoapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lists")
public class TodoListController {

    private final TodoListService todoListService;
    private final TodoItemService todoItemService;
    private final UserService userService;

    public TodoListController(TodoListService todoListService,
                               TodoItemService todoItemService,
                               UserService userService) {
        this.todoListService = todoListService;
        this.todoItemService = todoItemService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername());
    }

    @GetMapping("/{id}")
    public String viewList(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        User user = getCurrentUser(principal);
        var list = todoListService.findByIdAndUser(id, user);
        model.addAttribute("list", list);
        long completed = list.getItems().stream().filter(i -> i.isCompleted()).count();
        model.addAttribute("completedCount", completed);
        model.addAttribute("totalCount", list.getItems().size());
        return "lists/view";
    }

    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id,
                           @RequestParam String content,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Item content cannot be empty.");
            return "redirect:/lists/" + id;
        }
        todoItemService.addItem(id, content, user);
        return "redirect:/lists/" + id;
    }

    @PostMapping("/{listId}/items/{itemId}/toggle")
    public String toggleItem(@PathVariable Long listId,
                              @PathVariable Long itemId,
                              @AuthenticationPrincipal UserDetails principal) {
        User user = getCurrentUser(principal);
        todoItemService.toggleItem(itemId, listId, user);
        return "redirect:/lists/" + listId;
    }

    @PostMapping("/{listId}/items/{itemId}/delete")
    public String deleteItem(@PathVariable Long listId,
                              @PathVariable Long itemId,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        todoItemService.deleteItem(itemId, listId, user);
        redirectAttributes.addFlashAttribute("success", "Item deleted.");
        return "redirect:/lists/" + listId;
    }
}
