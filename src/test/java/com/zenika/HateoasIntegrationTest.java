/**
 * 
 */
package com.zenika;

import org.codehaus.jackson.JsonNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.hateoas.Link;
import org.springframework.web.client.RestTemplate;

import com.zenika.model.Contact;

/**
 * 
 * @author acogoluegnes
 *
 */
public class HateoasIntegrationTest {
	
	private static RestTemplate tpl = new RestTemplate();
	
	private final String BASE_URL = "http://localhost:8080/hateoas/zen-contact/";
	
	private static Server server;
	
	@BeforeClass public static void setUp() throws Exception {
		startServer();
	}
	
	@AfterClass public static void tearDown() throws Exception {
		server.stop();
	}
	
	@Test public void selectContacts() throws Exception {
		JsonNode nodes = tpl.getForObject(BASE_URL+"contacts", JsonNode.class);
		int totalElements = 12;
		Assert.assertEquals(totalElements,nodes.size());
		JsonNode node = nodes.get(0);
		JsonNode detailLink = node.get("id");
		String detailUrl = detailLink.get("href").getTextValue();
		
		Contact contact = tpl.getForObject(detailUrl,Contact.class);
		Assert.assertTrue(detailUrl.endsWith(contact.getId().toString()));
	}
	
	@Test public void selectContactsPages() throws Exception {
		String pageUrl = BASE_URL+"contacts/pages?page={page}&size={size}";
		int pageSize = 5;
		// first page
		JsonNode page = tpl.getForObject(
				pageUrl, 
				JsonNode.class,
				0,pageSize
		);
		Assert.assertEquals(page.get("content").size(), pageSize);
		int totalElements = page.get("totalElements").getIntValue();
		Assert.assertNull(getLink(page,Link.REL_PREVIOUS));
		pageUrl = getLink(page,Link.REL_NEXT);
		Assert.assertNotNull(pageUrl);
		// second page
		page = tpl.getForObject(pageUrl, JsonNode.class);
		Assert.assertEquals(page.get("content").size(), pageSize);
		pageUrl = getLink(page,Link.REL_NEXT);
		// third page
		page = tpl.getForObject(pageUrl, JsonNode.class);
		Assert.assertEquals(page.get("content").size(), totalElements%pageSize);
		Assert.assertNull(getLink(page,Link.REL_NEXT));
	}
	
	private String getLink(JsonNode node,String rel) {
		/* page structure:
		 { "links":[
           {"rel":"next","href":"http://localhost:8080/hateoas/zen-contact/contacts/pages?page=1&size=5"},
           {"rel":"first","href":"http://localhost:8080/hateoas/zen-contact/contacts/pages?page=0&size=5"},
           {"rel":"last","href":"http://localhost:8080/hateoas/zen-contact/contacts/pages?page=2&size=5"},
           {"rel":"self","href":"http://localhost:8080/hateoas/zen-contact/contacts/pages?page=0&size=5"}
           ],
           "content":[ ... ]
         }
		*/
		JsonNode links = node.get("links");
		if(links == null) {
			return null;
		} else {
			for(JsonNode link : links) {
				if(rel.equals(link.get("rel").getTextValue())) {
					return link.get("href").getTextValue();
				}
			}
			return null;
		}
	}
	
	private static void startServer() throws Exception {
		server = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(8080);
		connector.setHost("127.0.0.1");
		server.addConnector(connector);

		String app = "hateoas";
		
		WebAppContext wac = new WebAppContext();
		wac.setContextPath("/"+app);
		wac.setWar("./src/main/webapp");
		server.setHandler(wac);
		server.setStopAtShutdown(true);

		server.start();
	}

}
