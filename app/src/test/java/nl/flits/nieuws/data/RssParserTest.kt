package nl.flits.nieuws.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RssParserTest {

    private fun parse(xml: String) = RssParser.parse(xml.byteInputStream(), "UTF-8")

    @Test
    fun parsesRss2WithEnclosure() {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
              <channel><title>NOS</title>
                <item>
                  <title><![CDATA[Kabinet presenteert plannen]]></title>
                  <link>https://nos.nl/artikel/1</link>
                  <description><![CDATA[<p>Het kabinet <b>komt</b> met plannen.</p>]]></description>
                  <pubDate>Fri, 06 Nov 2026 10:15:00 +0100</pubDate>
                  <enclosure url="http://cdn.nos.nl/img.jpg" type="image/jpeg" length="0"/>
                </item>
              </channel>
            </rss>"""
        val items = parse(xml)
        assertEquals(1, items.size)
        val i = items[0]
        assertEquals("Kabinet presenteert plannen", i.title)
        assertEquals("https://nos.nl/artikel/1", i.link)
        assertEquals("Het kabinet komt met plannen.", i.summary)
        assertEquals("https://cdn.nos.nl/img.jpg", i.imageUrl)
        assertNotNull(i.publishedAt)
    }

    @Test
    fun parsesMediaContentAndImgFallback() {
        val xml = """<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/">
              <channel>
                <item>
                  <title>Met media</title><link>https://a.nl/1</link>
                  <media:content url="https://img.a.nl/1.jpg" medium="image"/>
                </item>
                <item>
                  <title>Met img</title><link>https://a.nl/2</link>
                  <description>&lt;img src="https://img.a.nl/2.png"&gt; tekst</description>
                </item>
              </channel>
            </rss>"""
        val items = parse(xml)
        assertEquals("https://img.a.nl/1.jpg", items[0].imageUrl)
        assertEquals("https://img.a.nl/2.png", items[1].imageUrl)
        assertEquals("tekst", items[1].summary)
    }

    @Test
    fun parsesAtom() {
        val xml = """<feed xmlns="http://www.w3.org/2005/Atom">
              <title>The Verge</title>
              <entry>
                <title type="html">New phone &amp; more</title>
                <link rel="alternate" type="text/html" href="https://www.theverge.com/x"/>
                <published>2026-11-06T09:00:00-05:00</published>
                <summary type="html">Short</summary>
              </entry>
            </feed>"""
        val items = parse(xml)
        assertEquals(1, items.size)
        assertEquals("New phone & more", items[0].title)
        assertEquals("https://www.theverge.com/x", items[0].link)
        assertTrue(items[0].publishedAt!! > 0)
    }

    @Test
    fun parsesDates() {
        assertNotNull(RssParser.parseDate("Fri, 6 Nov 2026 10:15:00 GMT"))
        assertNotNull(RssParser.parseDate("2026-11-06T10:15:00Z"))
        assertNotNull(RssParser.parseDate("Fri, 06 Nov 2026 10:15:00 +0100"))
    }

    @Test
    fun everyCategoryHasFeeds() {
        Category.entries.forEach { c ->
            assertTrue("Geen feeds voor ${c.label}", FeedCatalog.forCategory(c).isNotEmpty())
        }
        assertTrue(FeedCatalog.feeds.all { it.url.startsWith("https://") })
    }
}
