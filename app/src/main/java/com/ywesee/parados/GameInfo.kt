package com.ywesee.parados

data class GameVariant(
    val filename: String,
    val label: String
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
                description = "A 21st century successor to Rushhour. Hop through the outback collecting goodies \u2014 with all kinds of different ways to play!",
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
                description = "Othello on steroids! Area control on a random board, numbered discs and a flipping mechanism that invites all kinds of devious strategies."
            ),
            GameInfo(
                filename = "divided_loyalties.html",
                title = "Divided Loyalties",
                players = "2 Players \u00b7 Strategy",
                description = "Connect 4 with 6 colours. Your colour is always loyal, your opponent's never is \u2014 and the other 4? Sometimes they are, sometimes they aren't."
            ),
            GameInfo(
                filename = "democracy_remote.html",
                title = "Democracy in Space",
                players = "2+ Players \u00b7 Remote Multiplayer",
                description = "Based on the US Electoral College concept. Area majority culled to its essence \u2014 a gentle opening transforms into a nail biting race!"
            ),
            GameInfo(
                filename = "frankenstein.html",
                title = "Frankenstein \u2014 Where's that green elbow?",
                players = "1\u20134 Players \u00b7 Memory",
                description = "Short, sweet, and frankly memorable! For 1\u20134 players, age 7 and up. Don't be surprised if the youngest player wins!"
            ),
            GameInfo(
                filename = "rainbow_blackjack.html",
                title = "Rainbow Blackjack",
                players = "2 Players \u00b7 Strategy",
                description = "Colorful 21! Build 6 colored towers, trying to get as close to 21 as possible. Gray jokers add a devious twist.",
                variants = listOf(
                    GameVariant("rainbow_blackjack.html", "Deutsch"),
                    GameVariant("rainbow_blackjack_en.html", "English"),
                    GameVariant("rainbow_blackjack_remote.html", "Remote")
                )
            ),
            GameInfo(
                filename = "makalaina.html",
                title = "MAKA LAINA",
                players = "2 Players \u00b7 Strategy",
                description = "The battle is on from the first turn! Plan from the get go, evolve your strategy \u2014 but stay flexible. Even a small shift can have consequences.",
                variants = listOf(
                    GameVariant("makalaina.html", "Local"),
                    GameVariant("makalaina_remote.html", "Remote")
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
                "democracy_remote.html",
                "frankenstein.html",
                "rainbow_blackjack.html", "rainbow_blackjack_en.html",
                "rainbow_blackjack_remote.html",
                "makalaina.html", "makalaina_remote.html"
            )
    }
}
