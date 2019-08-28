package fastily.jwiki.test;


import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import fastily.jwiki.core.NS;

/**
 * Unit tests for WAction. Mocks cases where user is anonymous.
 * 
 * @author Fastily
 *
 */
public class ActionTest extends BaseMockTemplateTest
{
	/**
	 * Sanity check to make sure the mock Wiki object is properly initialized.
	 * @throws InterruptedException 
	 */
	@Test
	public void testInitializationForSanity() throws InterruptedException
	{
		assertEquals("File:Test.jpg", wiki.convertIfNotInNS("Test.jpg", NS.FILE));

			assertTrue(server.takeRequest(2, TimeUnit.SECONDS).getHeader("User-Agent").contains("jwiki"));
			
			
		assertEquals(NS.FILE.v, wiki.whichNS("File:Test.jpg").v);
		assertEquals(NS.MAIN.v, wiki.whichNS("hello").v);
	}

	/**
	 * Test editing
	 */
	@Test
	public void testEdit()
	{
		addResponse("mockSuccessEdit");
		assertTrue(wiki.edit("Wikipedia:Sandbox", "Hello, World!", "This is a test"));
	}

	/**
	 * Tests prepending and appending text via edit.
	 */
	@Test
	public void testAddText()
	{
		addResponse("mockSuccessEdit");
		addResponse("mockSuccessEdit");
		assertTrue(wiki.addText("Wikipedia:Sandbox", "Appending text!", "test", true));
		assertTrue(wiki.addText("Wikipedia:Sandbox", "Appending text!", "test", false));
	}

	/**
	 * Tests uploading of files
	 */
	@Test
	public void testUpload()
	{
		addResponse("mockChunkedUpload");
		addResponse("mockFileUnstash");

		try
		{
			assertTrue(wiki.upload(Paths.get(getClass().getResource("uploadTestFile.svg").toURI()), "TestSVG.svg", "desc", "summary"));
		}
		catch (Throwable e)
		{
		    e.printStackTrace();
			Assert.fail("Should never reach here - is the classpath messed up or a test resource missing?");
		}
	}

	/**
	 * Tests purging of pages
	 */
	@Test
	public void testPurge()
	{
		addResponse("mockPagePurge");
		wiki.purge("Foo", "Test", "Wikipedia:Sandbox");
	}
}