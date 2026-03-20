package com.ywesee.parados

data class GameVariant(
    val filename: String,
    val label: String,
    val url: String? = null
)

data class GameInfo(
    val filename: String,
    val title: String,
    val players: String,
    val description: String,
    val variants: List<GameVariant> = emptyList()
) {
    companion object {
        fun allGames(): List<GameInfo> = listOf(
            GameInfo(
                filename = "kangaroo.html",
                title = "DUK \u2014 The Impatient Kangaroo",
                players = "1 Player \u00b7 Puzzle",
                description = "The plan: to create a 21st century successor to the super-hit solo puzzle game Rushhour. Many players think that hopping through the outback collecting goodies is more fun than trying to shove your way through traffic? Another advantage \u2014 thanks to the program, there's all kinds of different ways to play:-)",
                variants = listOf(
                    GameVariant("kangaroo.html", "DE"),
                    GameVariant("kangaroo_en.html", "EN"),
                    GameVariant("kangaroo_jp.html", "JP"),
                    GameVariant("kangaroo_cn.html", "CN"),
                    GameVariant("kangaroo_ua.html", "UA")
                )
            ),
            GameInfo(
                filename = "capovolto.html",
                title = "Capovolto",
                players = "2 Players \u00b7 Strategy",
                description = "The classic game of Othello \u2014 on steroids! Add in area control on a random board, numbered discs and a flipping mechanism that is light years ahead of the original, inviting all kinds of devious strategies, and designed to make your brain go all sorts of places it hasn't been before:-)"
            ),
            GameInfo(
                filename = "divided_loyalties.html",
                title = "Divided Loyalties",
                players = "2 Players \u00b7 Strategy",
                description = "Many turns are offensive AND defensive and each one may have long term consequences! It's connect 4, but with 6 colours. Your colour is always loyal to you, your opponent's never is... and the other 4? Sometimes they are, and sometimes they aren't. Tiles can even be loyal in one direction, AND disloyal in another! Not for the faint of heart...."
            ),
            GameInfo(
                filename = "democracy.html",
                title = "Democracy in Space",
                players = "2+ Players \u00b7 Strategy",
                variants = listOf(
                    GameVariant("democracy.html", "Play"),
                    GameVariant("democracy_remote.html", "Remote", url = "https://game.ywesee.com/parados/democracy_remote.html")
                ),
                description = "Based on the concept of the US Electoral College (parliamentary systems also have it). Area majority culled to its essence. A gentle opening suddenly transforms into a nail biting race to the finish! The tie breaker condition needs to be kept in mind, but you won't know for a while if you'll need it this time...."
            ),
            GameInfo(
                filename = "frankenstein.html",
                title = "Frankenstein \u2014 Where's that green elbow?",
                players = "1\u20134 Players \u00b7 Memory",
                description = "This is even shorter and sweeter than Rainbow. For 1\u20134 players, it's a \"frankly memorable\" game (you'll get the pun when you play it). Like most of its colleagues here at Think Ahead, it is so much easier to play online. Age recommendation \u2014 7 years and up. Don't be surprised if the youngest player wins:-))."
            ),
            GameInfo(
                filename = "rainbow_blackjack.html",
                title = "Rainbow Blackjack",
                players = "2 Players \u00b7 Strategy",
                description = "Colorful 21! Two players build 6 colored towers, trying to get as close to 21 as possible \u2014 like Blackjack, but with colored stones. This game is easier to play than to describe:-) Arrange your stones in a grid, pick rows wisely, and announce just enough to keep your opponent guessing. Gray jokers add a devious twist...",
                variants = listOf(
                    GameVariant("rainbow_blackjack.html", "Deutsch"),
                    GameVariant("rainbow_blackjack_en.html", "English"),
                    GameVariant("rainbow_blackjack_remote.html", "Remote", url = "https://game.ywesee.com/parados/rainbow_blackjack_remote.html")
                )
            ),
            GameInfo(
                filename = "makalaina.html",
                title = "MAKA LAINA",
                players = "2 Players \u00b7 Strategy",
                description = "It's the first turn and the battle is on! No time to get warmed up in MakaLaina:-) You need to be planning from the get go, evolving your long term strategy \u2014 but staying flexible. The constant influx of new discs means that even a small shift can have consequences...",
                variants = listOf(
                    GameVariant("makalaina.html", "Play"),
                    GameVariant("makalaina_remote.html", "Remote", url = "https://game.ywesee.com/parados/makalaina_remote.html")
                )
            )
        )

        val allFilenames: List<String>
            get() = listOf(
                "index.html",
                "kangaroo.html", "kangaroo_en.html", "kangaroo_jp.html",
                "kangaroo_cn.html", "kangaroo_ua.html",
                "capovolto.html",
                "divided_loyalties.html",
                "democracy.html", "democracy_remote.html",
                "frankenstein.html",
                "rainbow_blackjack.html", "rainbow_blackjack_en.html",
                "rainbow_blackjack_remote.html",
                "makalaina.html", "makalaina_remote.html"
            )
    }
}
