package com.rackspace.cloud.api.util.sax;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class BookInfoXmlSaxHandlerTest {
	
	
	
	@Test
	public void testHandler(){
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			BookInfoXmlSaxHandler handler=new BookInfoXmlSaxHandler();
			
			InputStream cdnsDevguideBookinfoXML=this.getClass().getClassLoader().getResourceAsStream("cdns-devguide-bookinfo.xml");
			
			//Make sure we cdns-devguide-bookinfo.xml exists
			Assert.assertNotNull(cdnsDevguideBookinfoXML);
			
			try {
				saxParser.parse(cdnsDevguideBookinfoXML, handler);
				Assert.assertEquals("dns-docs", handler.getArtifactId());
				Assert.assertEquals("com.rackspace.cloud.dns.api", handler.getGroupId());
				Assert.assertEquals("com.rackspace.cloud.dns.api---dns-docs", handler.getGroupAndArtifact().getName());
				Assert.assertEquals("Cloud DNS API Docs", handler.getPomName());
				Assert.assertEquals("API Developer Guide",handler.getDisplayName());
				Assert.assertEquals("Cloud DNS Developer Guide", handler.getDocName());
				Assert.assertNull(handler.getJenkinsJobName());
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
