package nl.flits.nieuws.data

/** Nieuwscategorie zoals getoond in de tabbalk. */
enum class Category(val id: String, val label: String) {
    TOP("top", "Voorpagina"),
    BINNENLAND("binnenland", "Binnenland"),
    BUITENLAND("buitenland", "Buitenland"),
    POLITIEK("politiek", "Politiek"),
    ECONOMIE("economie", "Economie"),
    SPORT("sport", "Sport"),
    VOETBAL("voetbal", "Voetbal"),
    TECH("tech", "Tech"),
    WETENSCHAP("wetenschap", "Wetenschap"),
    GEZONDHEID("gezondheid", "Gezondheid"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    CULTUUR("cultuur", "Cultuur & Media"),
    OPMERKELIJK("opmerkelijk", "Opmerkelijk"),
    WERELD("wereld", "World (EN)");

    companion object {
        fun fromId(id: String?): Category? = entries.firstOrNull { it.id == id }
    }
}

/** Een nieuwsuitgever. [color] wordt gebruikt voor het bron-label. */
data class Source(val id: String, val name: String, val color: Long)

/** Eén RSS-/Atom-feed van een bron binnen een categorie. */
data class Feed(val source: Source, val category: Category, val url: String)

object Sources {
    val NOS = Source("nos", "NOS", 0xFFE4002B)
    val NU = Source("nu", "NU.nl", 0xFFFFB400)
    val TELEGRAAF = Source("telegraaf", "De Telegraaf", 0xFF0A4DA2)
    val AD = Source("ad", "AD", 0xFFD10A11)
    val VOLKSKRANT = Source("volkskrant", "de Volkskrant", 0xFF1E1E1E)
    val NRC = Source("nrc", "NRC", 0xFF2B2B2B)
    val TROUW = Source("trouw", "Trouw", 0xFF0062A8)
    val PAROOL = Source("parool", "Het Parool", 0xFFD4002B)
    val RTL = Source("rtl", "RTL Nieuws", 0xFF0F1E5A)
    val TWEAKERS = Source("tweakers", "Tweakers", 0xFF8E1F2A)
    val ANDROIDPLANET = Source("androidplanet", "Androidplanet", 0xFF3DDC84)
    val ICULTURE = Source("iculture", "iCulture", 0xFF5AA0E6)
    val SCIENTIAS = Source("scientias", "Scientias", 0xFF0B7A75)
    val VOETBALZONE = Source("voetbalzone", "Voetbalzone", 0xFF0E9F4F)
    val VRT = Source("vrt", "VRT NWS", 0xFF5B2E91)
    val BBC = Source("bbc", "BBC News", 0xFFBB1919)
    val GUARDIAN = Source("guardian", "The Guardian", 0xFF052962)
    val ALJAZEERA = Source("aljazeera", "Al Jazeera", 0xFFFA9000)
    val VERGE = Source("verge", "The Verge", 0xFF5200FF)

    val all = listOf(
        NOS, NU, TELEGRAAF, AD, VOLKSKRANT, NRC, TROUW, PAROOL, RTL,
        TWEAKERS, ANDROIDPLANET, ICULTURE, SCIENTIAS, VOETBALZONE, VRT,
        BBC, GUARDIAN, ALJAZEERA, VERGE,
    )

    fun byId(id: String): Source? = all.firstOrNull { it.id == id }
}

/** Alle gekoppelde RSS-feeds, per categorie. */
object FeedCatalog {
    private fun nos(slug: String) = "https://feeds.nos.nl/$slug"
    private fun nu(slug: String) = "https://www.nu.nl/rss/$slug"
    private fun ad(slug: String) = "https://www.ad.nl/$slug/rss.xml"
    private fun vk(slug: String) = "https://www.volkskrant.nl/$slug/rss.xml"
    private fun tg(slug: String) = "https://www.telegraaf.nl/$slug/rss"

    val feeds: List<Feed> = buildList {
        fun add(s: Source, c: Category, url: String) = add(Feed(s, c, url))
        with(Sources) {
            // Voorpagina
            add(NOS, Category.TOP, nos("nosnieuwsalgemeen"))
            add(NU, Category.TOP, nu("Algemeen"))
            add(TELEGRAAF, Category.TOP, "https://www.telegraaf.nl/rss")
            add(AD, Category.TOP, ad("home"))
            add(VOLKSKRANT, Category.TOP, vk("voorpagina"))
            add(NRC, Category.TOP, "https://www.nrc.nl/rss/")
            add(TROUW, Category.TOP, "https://www.trouw.nl/voorpagina/rss.xml")
            add(PAROOL, Category.TOP, "https://www.parool.nl/voorpagina/rss.xml")
            add(RTL, Category.TOP, "https://www.rtlnieuws.nl/rss.xml")

            // Binnenland
            add(NOS, Category.BINNENLAND, nos("nosnieuwsbinnenland"))
            add(NU, Category.BINNENLAND, nu("Binnenland"))
            add(AD, Category.BINNENLAND, ad("binnenland"))
            add(TELEGRAAF, Category.BINNENLAND, tg("nieuws/binnenland"))
            add(VOLKSKRANT, Category.BINNENLAND, vk("nederland"))

            // Buitenland
            add(NOS, Category.BUITENLAND, nos("nosnieuwsbuitenland"))
            add(NU, Category.BUITENLAND, nu("Buitenland"))
            add(AD, Category.BUITENLAND, ad("buitenland"))
            add(TELEGRAAF, Category.BUITENLAND, tg("nieuws/buitenland"))
            add(VOLKSKRANT, Category.BUITENLAND, vk("buitenland"))
            add(VRT, Category.BUITENLAND, "https://www.vrt.be/vrtnws/nl.rss.articles.xml")

            // Politiek
            add(NOS, Category.POLITIEK, nos("nosnieuwspolitiek"))
            add(NU, Category.POLITIEK, nu("Politiek"))
            add(AD, Category.POLITIEK, ad("politiek"))
            add(VOLKSKRANT, Category.POLITIEK, vk("politiek"))

            // Economie
            add(NOS, Category.ECONOMIE, nos("nosnieuwseconomie"))
            add(NU, Category.ECONOMIE, nu("Economie"))
            add(AD, Category.ECONOMIE, ad("geld"))
            add(TELEGRAAF, Category.ECONOMIE, tg("financieel"))
            add(VOLKSKRANT, Category.ECONOMIE, vk("economie"))

            // Sport
            add(NOS, Category.SPORT, nos("nossportalgemeen"))
            add(NU, Category.SPORT, nu("Sport"))
            add(AD, Category.SPORT, ad("sport"))
            add(TELEGRAAF, Category.SPORT, tg("sport"))

            // Voetbal
            add(NOS, Category.VOETBAL, nos("nosvoetbal"))
            add(NU, Category.VOETBAL, nu("Voetbal"))
            add(AD, Category.VOETBAL, ad("voetbal"))
            add(VOETBALZONE, Category.VOETBAL, "https://www.voetbalzone.nl/rss/rss.xml")

            // Tech
            add(NOS, Category.TECH, nos("nosnieuwstech"))
            add(NU, Category.TECH, nu("Tech"))
            add(TWEAKERS, Category.TECH, "https://feeds.feedburner.com/tweakers/mixed")
            add(ANDROIDPLANET, Category.TECH, "https://www.androidplanet.nl/feed/")
            add(ICULTURE, Category.TECH, "https://www.iculture.nl/feed/")
            add(AD, Category.TECH, ad("tech"))

            // Wetenschap
            add(NU, Category.WETENSCHAP, nu("Wetenschap"))
            add(SCIENTIAS, Category.WETENSCHAP, "https://www.scientias.nl/feed/")
            add(VOLKSKRANT, Category.WETENSCHAP, vk("wetenschap"))

            // Gezondheid
            add(NU, Category.GEZONDHEID, nu("Gezondheid"))
            add(VOLKSKRANT, Category.GEZONDHEID, vk("gezondheid"))

            // Entertainment
            add(NU, Category.ENTERTAINMENT, nu("Entertainment"))
            add(AD, Category.ENTERTAINMENT, ad("show"))
            add(TELEGRAAF, Category.ENTERTAINMENT, tg("entertainment"))

            // Cultuur & media
            add(NOS, Category.CULTUUR, nos("nosnieuwscultuurenmedia"))
            add(VOLKSKRANT, Category.CULTUUR, vk("cultuur-media"))
            add(NU, Category.CULTUUR, nu("Media"))

            // Opmerkelijk
            add(NOS, Category.OPMERKELIJK, nos("nosnieuwsopmerkelijk"))
            add(NU, Category.OPMERKELIJK, nu("Opmerkelijk"))
            add(AD, Category.OPMERKELIJK, ad("bizar"))

            // Internationaal (Engelstalig)
            add(BBC, Category.WERELD, "https://feeds.bbci.co.uk/news/world/rss.xml")
            add(GUARDIAN, Category.WERELD, "https://www.theguardian.com/world/rss")
            add(ALJAZEERA, Category.WERELD, "https://www.aljazeera.com/xml/rss/all.xml")
            add(VERGE, Category.WERELD, "https://www.theverge.com/rss/index.xml")
        }
    }

    fun forCategory(category: Category): List<Feed> = feeds.filter { it.category == category }
}
