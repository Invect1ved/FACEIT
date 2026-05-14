package com.example.faceit.data.local

import com.example.faceit.model.Match
import com.example.faceit.model.MatchResult
import com.example.faceit.model.Player

/**
 * Демо-данные: добавляет каждого «шаблонного» игрока и его матчи только если
 * игрока с таким никнеймом ещё нет. Так на главном появятся все демо-игроки,
 * даже если в базе уже был один пользовательский профиль.
 */
object InitialDataSeed {

    suspend fun ensureDemoData(playerDao: PlayerDao, matchDao: MatchDao) {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val demos = listOf(
            DemoBundle(
                player = Player(
                    nickname = "NiKo_Demo",
                    elo = 2850,
                    level = 9,
                    faceitUrl = "https://www.faceit.com/ru/players/NiKo",
                    comment = "Демо: рифлер, много фрагов на картах с длинными дистанциями."
                ),
                matches = listOf(
                    MatchStub("de_inferno", MatchResult.WIN, 24, 16, now - dayMs * 2),
                    MatchStub("de_mirage", MatchResult.LOSS, 17, 21, now - dayMs),
                    MatchStub("de_ancient", MatchResult.WIN, 29, 19, now - 6 * 3_600_000L)
                )
            ),
            DemoBundle(
                player = Player(
                    nickname = "ZywOo_Scout",
                    elo = 3010,
                    level = 10,
                    faceitUrl = "https://www.faceit.com/ru/players/ZywOo",
                    comment = "Демо: AWP, стабильный K/D."
                ),
                matches = listOf(
                    MatchStub("de_overpass", MatchResult.WIN, 31, 14, now - dayMs * 3),
                    MatchStub("de_vertigo", MatchResult.WIN, 26, 17, now - dayMs)
                )
            ),
            DemoBundle(
                player = Player(
                    nickname = "apex_igl",
                    elo = 2420,
                    level = 7,
                    faceitUrl = "https://www.faceit.com/ru/players/apex",
                    comment = "Демо: IGL, меньше рейтинга — больше вызовов."
                ),
                matches = listOf(
                    MatchStub("de_nuke", MatchResult.LOSS, 14, 22, now - dayMs * 4),
                    MatchStub("de_anubis", MatchResult.WIN, 21, 20, now - dayMs * 2)
                )
            ),
            DemoBundle(
                player = Player(
                    nickname = "Новичок_Лаб8",
                    elo = 1250,
                    level = 3,
                    faceitUrl = "",
                    comment = "Демо: пустая ссылка — можно отредактировать в форме."
                ),
                matches = listOf(
                    MatchStub("de_dust2", MatchResult.LOSS, 11, 18, now - 12 * 3_600_000L)
                )
            )
        )

        for (demo in demos) {
            if (playerDao.getByNickname(demo.player.nickname) != null) continue
            val id = playerDao.insert(demo.player)
            for (stub in demo.matches) {
                matchDao.insert(
                    Match(
                        playerId = id,
                        mapName = stub.mapName,
                        result = stub.result,
                        kills = stub.kills,
                        deaths = stub.deaths,
                        date = stub.date
                    )
                )
            }
        }
    }

    private data class DemoBundle(
        val player: Player,
        val matches: List<MatchStub>
    )

    private data class MatchStub(
        val mapName: String,
        val result: MatchResult,
        val kills: Int,
        val deaths: Int,
        val date: Long
    )
}
