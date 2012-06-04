/**
 * 
 */
package com.zenika.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zenika.model.Contact;
import com.zenika.repository.ContactRepository;

/**
 * @author acogoluegnes
 *
 */
@Controller
@RequestMapping("/contacts")
public class ContactController {
	
	@Autowired ContactRepository contactRepository;
	
	@RequestMapping(value="/{id}",method=RequestMethod.GET)
	public ResponseEntity<Contact> contact(@PathVariable Long id) {		
		Contact contact = contactRepository.findOne(id);
		ResponseEntity<Contact> response = new ResponseEntity<Contact>(
			contact,
			contact == null ? HttpStatus.NOT_FOUND : HttpStatus.OK
		);
		return response;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	@ResponseBody 
	public List<ShortContact> contacts() {
		List<Contact> contacts = contactRepository.findAll();
		List<ShortContact> resources = new ArrayList<ShortContact>(contacts.size());
		for(Contact contact : contacts) {
			ShortContact resource = new ShortContact();
			resource.setFirstname(contact.getFirstname());
			resource.setLastname(contact.getLastname());
			Link detail = linkTo(ContactController.class).slash(contact.getId()).withSelfRel();
			resource.add(detail);
			resources.add(resource);
		}
		return resources;
	}
	
	@RequestMapping(value="/pages",method=RequestMethod.GET)
	@ResponseBody 
	public PageResource<Contact> contactsPages(@RequestParam int page,@RequestParam int size) {
		Pageable pageable = new PageRequest(
			page,size,new Sort("id")
		);
		Page<Contact> pageResult = contactRepository.findAll(pageable);
		return new PageResource<Contact>(pageResult,"page","size");
	}
	
	public static class ShortContact extends ResourceSupport {
	
		private String firstname,lastname;

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}
	
	}
}
