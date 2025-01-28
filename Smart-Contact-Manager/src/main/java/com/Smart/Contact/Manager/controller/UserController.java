package com.Smart.Contact.Manager.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.Smart.Contact.Manager.dao.UserRepository;
import com.Smart.Contact.Manager.entities.Contact;
import com.Smart.Contact.Manager.entities.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository repository;

	// Method for adding common data to response.
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("username" + username);
		// Get the user using username
		User user = repository.getUserByUserName(username);
		System.out.println("user details : " + user);
		model.addAttribute(user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

//	Open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			// Get the current authenticated user
			String name = principal.getName();
			User user = this.repository.getUserByUserName(name);

			// Check if the file is empty
			if (file.isEmpty()) {
				session.setAttribute("errorMessage", "File is empty. Please upload a valid image.");
			} else {
				// Define the path for saving the uploaded file
				String uploadDir = "src/main/resources/static/img"; // Ensure this folder exists
				String fileName = file.getOriginalFilename(); // Get the original file name

				// Create the path to save the file
				File saveFile = new File(uploadDir, fileName);
				Path path = saveFile.toPath();

				// Check if the directory exists, if not, create it
				if (!saveFile.getParentFile().exists()) {
					saveFile.getParentFile().mkdirs(); // Create necessary directories
				}

				// Save the uploaded file to the specified directory
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded to: " + path);

				// Set the contact's image name (this will be saved in the database)
				contact.setImage(fileName); // Store the file name (not the file itself)
			}

			// Associate the contact with the user
			user.getContacts().add(contact);
			contact.setUser(user);

			// Save the user (which also saves the contact since it's associated with the
			// user)
			this.repository.save(user);
			System.out.println("Contact added to database: " + contact);

			// Success message
			session.setAttribute("successMessage", "Contact added successfully!");

		} catch (IOException e) {
			System.out.println("Error during file upload: " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("errorMessage", "Error during file upload: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("General error: " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
		}

		// Return the view name (could be a redirect or a different view based on your
		// flow)
		return "normal/add_contact_form"; // Return the view name
	}

}
