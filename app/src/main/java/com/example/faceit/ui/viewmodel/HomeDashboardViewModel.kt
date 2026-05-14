package com.example.faceit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceit.data.repository.PlayerRepository
import com.example.faceit.model.Match
import com.example.faceit.model.MatchResult
import com.example.faceit.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

data class MapStat(val mapName: String, val matches: Int)

data class HomeDashboardContent(
    val playerId: Long?,
    val nickname: String,
    val elo: Int,
    val level: Int,
    val roster: List<Player>,
    val maps: List<MapStat>,
    val eloHistory: List<Int>,
    val matchHistory: List<MatchHistoryRow>,
    val kd: Double,
    val winRatePercent: Int,
    val hsPercent: Int,
    val adr: Double,
    val matchesCount: Int,
    val wins: Int,
    val losses: Int,
    val isPlaceholder: Boolean
)

sealed interface HomeDashboardUiState {
    data object Loading : HomeDashboardUiState
    data class Ready(val content: HomeDashboardContent) : HomeDashboardUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    private val repository: PlayerRepository
) : ViewModel() {

    val uiState: StateFlow<HomeDashboardUiState> = repository.observePlayers()
        .flatMapLatest { players ->
            val me = players.minByOrNull { it.id }
            if (me == null) {
                flowOf(HomeDashboardUiState.Ready(buildPlaceholder(Random(System.nanoTime()), players)))
            } else {
                repository.observeMatches(me.id).map { matches ->
                    HomeDashboardUiState.Ready(buildForPlayer(me, matches, players))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeDashboardUiState.Loading
        )

    private fun buildForPlayer(player: Player, matches: List<Match>, allPlayers: List<Player>): HomeDashboardContent {
        val rnd = Random(player.id xor (matches.size.toLong() shl 20))
        val stats = combatStats(matches, rnd)
        val wins = matches.count { it.result == MatchResult.WIN }
        val losses = matches.size - wins
        val asc = matches.sortedBy { it.date }
        val sim = simulateEloSeries(player.elo, asc, rnd)
        return HomeDashboardContent(
            playerId = player.id,
            nickname = player.nickname,
            elo = player.elo,
            level = player.level,
            roster = allPlayers.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nickname }),
            maps = buildMapStats(matches, rnd),
            eloHistory = sim.points,
            matchHistory = buildMatchHistory(asc, sim),
            kd = stats.kd,
            winRatePercent = stats.winRatePercent,
            hsPercent = stats.hsPercent,
            adr = stats.adr,
            matchesCount = matches.size,
            wins = wins,
            losses = losses,
            isPlaceholder = false
        )
    }

    private fun buildPlaceholder(rnd: Random, allPlayers: List<Player>): HomeDashboardContent {
        val elo = rnd.nextInt(1750, 2460)
        val fakeMatches = List(rnd.nextInt(6, 14)) { idx ->
            Match(
                playerId = 0L,
                mapName = listOf("de_mirage", "de_inferno", "de_nuke", "de_overpass", "de_anubis").random(rnd),
                result = if (rnd.nextBoolean()) MatchResult.WIN else MatchResult.LOSS,
                kills = rnd.nextInt(14, 32),
                deaths = rnd.nextInt(10, 26),
                date = System.currentTimeMillis() - idx * 86_400_000L
            )
        }
        val stats = combatStats(fakeMatches, rnd)
        val wins = fakeMatches.count { it.result == MatchResult.WIN }
        val asc = fakeMatches.sortedBy { it.date }
        val sim = simulateEloSeries(elo, asc, rnd)
        return HomeDashboardContent(
            playerId = null,
            nickname = "Гость",
            elo = elo,
            level = rnd.nextInt(4, 9),
            roster = allPlayers.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nickname }),
            maps = buildMapStats(fakeMatches, rnd),
            eloHistory = sim.points,
            matchHistory = buildMatchHistory(asc, sim),
            kd = stats.kd,
            winRatePercent = stats.winRatePercent,
            hsPercent = stats.hsPercent,
            adr = stats.adr,
            matchesCount = fakeMatches.size,
            wins = wins,
            losses = fakeMatches.size - wins,
            isPlaceholder = true
        )
    }

    private data class EloSim(val points: List<Int>, val deltasAsc: List<Int>)

    /**
     * Кривая эло по хронологии матчей: победы дают +, поражения −, сила зависит от K/D матча;
     * последняя точка = текущее эло из профиля.
     */
    private fun simulateEloSeries(currentElo: Int, matchesAsc: List<Match>, rnd: Random): EloSim {
        if (matchesAsc.isEmpty()) {
            val n = 15
            val pts = MutableList(n) { currentElo }
            pts[n - 1] = currentElo
            val (lo, hi) = eloBand(currentElo, marginBelow = 900, marginAbove = 280)
            for (i in n - 2 downTo 0) {
                val swing = rnd.nextInt(-36, 41)
                pts[i] = (pts[i + 1] - swing).coerceIn(lo, hi)
            }
            pts[n - 1] = currentElo
            return EloSim(pts, emptyList())
        }
        val deltas = matchesAsc.map { m ->
            val kd = m.kills.toFloat() / m.deaths.coerceAtLeast(1)
            val win = m.result == MatchResult.WIN
            val core = if (win) rnd.nextInt(9, 28) else -rnd.nextInt(14, 38)
            val perf = ((kd - 1f) * 11f).roundToInt().coerceIn(-11, 15)
            (core + perf).coerceIn(-52, 44)
        }
        val (startLo, startHi) = eloBand(currentElo, marginBelow = 2000, marginAbove = 400)
        var start = (currentElo - deltas.sum()).coerceIn(startLo, startHi)
        val adj = deltas.toMutableList()
        var cur = start
        val series = mutableListOf<Int>()
        series.add(start)
        for (d in adj) {
            cur += d
            cur = cur.coerceIn(760, 4000)
            series.add(cur)
        }
        val fix = currentElo - cur
        if (adj.isNotEmpty()) {
            adj[adj.lastIndex] = adj.last() + fix
        }
        cur = start
        series.clear()
        series.add(start)
        for (d in adj) {
            cur += d
            cur = cur.coerceIn(760, 4000)
            series.add(cur)
        }
        if (series.isNotEmpty()) {
            series[series.lastIndex] = currentElo
        }
        return EloSim(series, adj)
    }

    /** Диапазон [lo; hi] вокруг текущего эло, всегда с lo ≤ hi (в т.ч. при низком эло). */
    private fun eloBand(center: Int, marginBelow: Int, marginAbove: Int): Pair<Int, Int> {
        var lo = (center - marginBelow).coerceAtLeast(0)
        var hi = (center + marginAbove).coerceAtMost(5000)
        if (lo > hi) {
            lo = center.coerceAtLeast(0)
            hi = center.coerceAtMost(5000)
        }
        return lo to hi
    }

    private fun buildMatchHistory(matchesAsc: List<Match>, sim: EloSim): List<MatchHistoryRow> {
        if (matchesAsc.isEmpty() || sim.deltasAsc.isEmpty()) return emptyList()
        return matchesAsc.indices.reversed().map { i ->
            val m = matchesAsc[i]
            MatchHistoryRow(
                mapName = m.mapName,
                result = m.result,
                kills = m.kills,
                deaths = m.deaths,
                dateMillis = m.date,
                eloDelta = sim.deltasAsc[i],
                eloAfter = sim.points[i + 1]
            )
        }
    }

    private data class CombatStats(
        val kd: Double,
        val winRatePercent: Int,
        val hsPercent: Int,
        val adr: Double
    )

    private fun combatStats(matches: List<Match>, rnd: Random): CombatStats {
        if (matches.isEmpty()) {
            return CombatStats(
                kd = rnd.nextDouble() * 0.46 + 0.92,
                winRatePercent = rnd.nextInt(48, 72),
                hsPercent = rnd.nextInt(38, 56),
                adr = rnd.nextDouble() * 24.0 + 72.0
            )
        }
        val k = matches.sumOf { it.kills }
        val d = matches.sumOf { it.deaths }.coerceAtLeast(1)
        val kd = k / d.toDouble()
        val wins = matches.count { it.result == MatchResult.WIN }
        val wr = ((100.0 * wins) / matches.size).roundToInt().coerceIn(0, 100)
        return CombatStats(
            kd = kd,
            winRatePercent = wr,
            hsPercent = rnd.nextInt(41, 59),
            adr = rnd.nextDouble() * 17.0 + 74.0
        )
    }

    private fun buildMapStats(matches: List<Match>, rnd: Random): List<MapStat> {
        val fromDb = matches
            .groupingBy { it.mapName }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { MapStat(it.key, it.value) }
            .toMutableList()
        if (fromDb.size >= 4) return fromDb.take(8)
        val filler = listOf("de_mirage", "de_inferno", "de_nuke", "de_anubis", "de_vertigo", "de_ancient")
            .shuffled(rnd)
            .map { m -> MapStat(m, rnd.nextInt(2, 14)) }
        val seen = fromDb.map { it.mapName }.toMutableSet()
        for (f in filler) {
            if (fromDb.size >= 6) break
            if (f.mapName !in seen) {
                fromDb.add(f)
                seen.add(f.mapName)
            }
        }
        return fromDb.sortedByDescending { it.matches }
    }
}
