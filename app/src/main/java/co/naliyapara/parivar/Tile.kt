package co.naliyapara.parivar

/**
 * Home screen no ek tile.
 * tabLabel = website na top menu ma je button no data-label hoy te (tab khole che)
 * path     = tab ne badle koi alag page (jem k poster-maker.html)
 */
data class Tile(
    val title: String,
    val emoji: String,
    val color: Int,
    val tabLabel: String? = null,
    val path: String? = null
)

val TILES = listOf(
    Tile("પરિવાર સમાચાર", "📰", 0xFFE53935.toInt(), tabLabel = "સમાચાર અને જાહેરાત"),
    Tile("સભ્યોની માહિતી", "👥", 0xFF1F6FB5.toInt(), tabLabel = "સમાજ"),
    Tile("મારો પ્રોફાઈલ", "👤", 0xFF1FA3D1.toInt(), tabLabel = "સભ્ય લોગીન"),

    Tile("ID કાર્ડ", "💳", 0xFF4CAF50.toInt(), tabLabel = "સભ્ય લોગીન"),
    Tile("ફોટો ગેલેરી", "🖼️", 0xFF5B2EA6.toInt(), tabLabel = "ગેલેરી"),
    Tile("જાહેરાત", "📢", 0xFF3CB04B.toInt(), tabLabel = "ધંધા ની જાહેરાત"),

    Tile("તહેવાર પોસ્ટર", "🎉", 0xFFE91E78.toInt(), path = "poster-maker.html"),
    Tile("નોટિફિકેશન", "🔔", 0xFF14A3A0.toInt(), tabLabel = "સમાચાર અને જાહેરાત"),
    Tile("ઇતિહાસ", "📖", 0xFFF57C00.toInt(), tabLabel = "કુટુંબ નો ઇતિહાસ"),

    Tile("સંચાલક મંડળ", "🧑‍🤝‍🧑", 0xFF1F5FBF.toInt(), tabLabel = "સંચાલક મંડળ"),
    Tile("મંતવ્ય", "💬", 0xFFF0705F.toInt(), tabLabel = "પ્રસ્તાવ"),
    Tile("લોગીન", "🔑", 0xFF1F5FBF.toInt(), tabLabel = "સભ્ય લોગીન")
)
