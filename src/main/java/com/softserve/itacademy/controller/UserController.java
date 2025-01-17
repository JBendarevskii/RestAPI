package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new User());
        return "create-user";
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "create-user";
        }
        user.setPassword(user.getPassword());
        user.setRole(roleService.readById(2));
        User newUser = userService.create(user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    @GetMapping("/{id}/read")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.id == #id")
    public String read(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/{id}/update")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.id == #id")
    public String update(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAll());
        return "update-user";
    }


    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model, @Validated @ModelAttribute("user") User user, @RequestParam("roleId") long roleId, BindingResult result) {
        User oldUser = userService.readById(id);
        if (result.hasErrors()) {
            user.setRole(oldUser.getRole());
            model.addAttribute("roles", roleService.getAll());
            return "update-user";
        }
        user.setRole(roleService.readById(roleId));
        userService.update(user);
        return "redirect:/users/" + id + "/read";
    }


    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.id == #id")
    public String delete(@PathVariable("id") long id, Principal principal) {
        User securedUser = userService.readByEmail(principal.getName());
        userService.delete(id);
        if(securedUser.getId() == id) {
            return "redirect:/logout";
        }
        return "redirect:/users/all";
    }

    @GetMapping("/all")
    public String getAll(Model model, Principal principal) {
        User user = userService.readByEmail(principal.getName());
        if(user.getRole().getName().equals("ADMIN")) {
            model.addAttribute("users", userService.getAll());
        } else {
            model.addAttribute("users", List.of(user));
        }
        return "users-list";
    }
}
