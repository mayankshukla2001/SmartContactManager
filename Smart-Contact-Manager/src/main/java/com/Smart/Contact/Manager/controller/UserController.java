package com.Smart.Contact.Manager.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.Smart.Contact.Manager.dao.ContactRepository;
import com.Smart.Contact.Manager.dao.UserRepository;
import com.Smart.Contact.Manager.entities.Contact;
import com.Smart.Contact.Manager.entities.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository repository;

	@Autowired
	private ContactRepository contactRepository;

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

	// Show contacts handler
//	@GetMapping("/show-contacts")
//	public String showContact(Model m, Principal principal)
//	{
//		m.addAttribute("title", "Show User Contacts");
//		String userName = principal.getName();
//		User user = this.repository.getUserByUserName(userName);
//		System.out.println("user : " + user);
////		List<Contact> contacts = user.getContacts();
//		
//		
//		List<Contact> contacts = this.contactRepository.findContactsByUser(user.getId());
//		System.out.println("List1 : " +contacts);
//		m.addAttribute("contacts", contacts);
//
//		return "normal/show_contacts";
//	}

	@GetMapping("/show-contacts")
	public String showContact(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "2") int size, Model model, Principal principal) {

		// Set up pagination with page number and size
		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending()); // Change sort as needed

		// Get the current user
		String userName = principal.getName();
		User user = this.repository.getUserByUserName(userName);

		// Fetch the paginated contacts for the user
		Page<Contact> contactsPage = this.contactRepository.findContactsByUser(user.getId(), pageable);

		// Add pagination data to the model
		model.addAttribute("contacts", contactsPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contactsPage.getTotalPages());
		model.addAttribute("totalContacts", contactsPage.getTotalElements());
		model.addAttribute("title", "Show User Contacts");

		return "normal/show_contacts";
	}

	// Delete contact handler
	@GetMapping("/delete-contact/{id}")
	public String deleteContact(@PathVariable("id") int contactId, Principal principal, HttpSession session) {
		try {
			// Get the current user
			String userName = principal.getName();
			User user = this.repository.getUserByUserName(userName);

			// Find the contact by ID
			Contact contact = this.contactRepository.findById(contactId).get();

			// Ensure the contact exists and belongs to the logged-in user
			if (contact != null && contact.getUser().getId() == user.getId()) {
				// Delete the contact (cascade deletes associated data if configured)
				user.getContacts().remove(contact);
				this.repository.save(user);
				// Optionally, delete the image from the file system if you are storing it
				String imagePath = "src/main/resources/static/img/" + contact.getImage();
				File imageFile = new File(imagePath);
				if (imageFile.exists()) {
					imageFile.delete(); // Delete the image file
				}

				session.setAttribute("successMessage", "Contact deleted successfully!");
			} else {
				session.setAttribute("errorMessage", "Contact not found or not authorized to delete.");
			}
		} catch (Exception e) {
			session.setAttribute("errorMessage", "Error deleting contact: " + e.getMessage());
			e.printStackTrace();
		}
		return "redirect:/user/show-contacts"; // Redirect back to the contacts list
	}

	// Open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") int contactId, Model m, Principal principal) {
		String userName = principal.getName();
		User user = this.repository.getUserByUserName(userName);
		Contact contact = this.contactRepository.findById(contactId).get();
		m.addAttribute("contact", contact); // Handle the case where contact is not found
		m.addAttribute("title", "Update User Contacts");
		return "normal/update_form"; // Return the view for editing the contact
	}

// Update form Handler
	@PostMapping("/process-update/{cid}")
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, @PathVariable("cid") int contactId, Principal principal, HttpSession httpSession) {
		try {
			// Fetch the current logged-in user
			String userName = principal.getName();
			User user = this.repository.getUserByUserName(userName);

			// Fetch the existing contact from the database using contactId
			Contact existingContact = this.contactRepository.findById(contactId)
					.orElseThrow(() -> new IllegalArgumentException("Contact not found"));

			// Ensure the contact belongs to the current user
			if (!existingContact.getUser().equals(user)) {
				// Redirect or handle the situation if contact doesn't belong to the user
				return "redirect:/access-denied"; // Handle this as necessary
			}

			// Update the existing contact with the new values from the form
			existingContact.setName(contact.getName());
			existingContact.setSecondName(contact.getSecondName());
			existingContact.setWork(contact.getWork());
			existingContact.setEmail(contact.getEmail());
			existingContact.setPhone(contact.getPhone());
			existingContact.setDescription(contact.getDescription());

			// Check if a new image was uploaded
			if (!file.isEmpty()) {
				// Delete the old image if exists
				String oldImage = existingContact.getImage();
				if (oldImage != null && !oldImage.isEmpty()) {
					// Construct the path for the old image
					String oldImagePath = "src/main/resources/static/img/" + oldImage;
					File oldFile = new File(oldImagePath);
					if (oldFile.exists()) {
						oldFile.delete(); // Delete the old image
					}
				}

				// Handle the image saving (e.g., save to a directory, update the image field)
				String imageFileName = saveProfileImage(file);
				existingContact.setImage(imageFileName); // Set the new image name in the contact
			}

			// Save the updated contact back to the database
			this.contactRepository.save(existingContact);
			httpSession.setAttribute("successMessage", "Your contact is successfully updated!");

			// Redirect back to the contacts list after update
			return "redirect:/user/show-contacts";
		} catch (Exception e) {
			// Handle any exceptions (e.g., log the error)
			e.printStackTrace();
			m.addAttribute("error", "An error occurred while updating the contact");
			return "errorPage"; // Redirect to an error page or show error message
		}
	}

	/**
	 * Saves the profile image to the server and returns the image file name.
	 */
	private String saveProfileImage(MultipartFile file) {
		try {
			// Define the upload directory and file name
			String uploadDir = "src/main/resources/static/img"; // Update with your correct path
			String fileName = file.getOriginalFilename(); // Get the original file name
			Path path = Paths.get(uploadDir + fileName);

			// Save the file to the defined directory
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			return fileName; // Return the saved file name to store in the database
		} catch (IOException e) {
			e.printStackTrace(); // Log the error
			throw new RuntimeException("Failed to upload image", e); // Or handle the error accordingly
		}
	}

}
