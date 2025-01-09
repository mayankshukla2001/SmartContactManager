package com.Smart.Contact.Manager.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.Smart.Contact.Manager.dao.UserRepository;
import com.Smart.Contact.Manager.entities.User;
import com.Smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("tittle", "Home - Smart Contact Manager");
		return "home";
	}
	

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("tittle", "About - Smart Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("tittle", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// Hander for registering user
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
	    try {
	    	System.out.println("Has Errors: " + result.hasErrors());  // Log if there are errors
	        // Check if there are validation errors
	    	if (result.hasErrors()) {
		    	System.out.println("itttt");

	    	    // Log the validation errors for debugging
	    	    System.out.println("Validation Errors: " + result.getAllErrors());

	    	    // You can also log each error in more detail
	    	    for (ObjectError error : result.getAllErrors()) {
	    	        System.out.println("Error: " + error.getDefaultMessage());
	    	    }

	    	    model.addAttribute("user", user);
	    	    return "signup";
	    	}

	        // Check if the email already exists in the database
	        if (userRepository.existsByEmail(user.getEmail())) {
	            // Add an error to the BindingResult
	            result.rejectValue("email", "email.exists", "Email is already in use.");

	            // Add the user object back to the model
	            model.addAttribute("user", user);

	            // Return to the signup page with email error
	            return "signup";
	        }

	        // Proceed if there are no validation errors or duplicate email
	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setPassword(passwordEncoder.encode(user.getPassword()));

	        // Save the user to the database
	        this.userRepository.save(user);

	        // Reset the form data after successful registration
	        model.addAttribute("user", new User());

	        // Clear any existing messages in the session
	        session.removeAttribute("message");

	        // Set a success message in the session
	        session.setAttribute("message", new Message("Registration successful! Please login.", "alert-success"));

	        // Return to the signup page with success message
	        return "signup"; // Or redirect to another page if needed

	    } catch (Exception e) {
	        // Log the error
	        e.printStackTrace();

	        // Add the user object to the model to preserve form data
	        model.addAttribute("user", user);

	        // Set an error message for the session
	        session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));

	        // Return to the signup page with error message
	        return "signup";
	    }
	}



}
