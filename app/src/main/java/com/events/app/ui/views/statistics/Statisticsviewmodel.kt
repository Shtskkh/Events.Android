package com.events.app.ui.views.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.local.EventRefreshBus
import com.events.app.data.remote.dto.ViewsDto
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

// ── Базовые модели ────────────────────────────────────────────────

data class PieSlice(val label: String, val count: Int, val color: Long)
data class BarItem(val label: String, val value: Int, val eventId: String = "")
data class ViewPoint(val date: String, val views: Int)

// ── Новые модели ──────────────────────────────────────────────────

/** Сколько мероприятий было в конкретном помещении */
data class PlaceStat(
    val placeId: Int,
    val locationId: Int = 0,
    val placeName: String,
    val locationName: String,
    val eventCount: Int,
    val totalCapacity: Int,    // вместимость помещения
    val avgFillRate: Float,    // средняя заполняемость 0..100
    val events: List<String>   // id событий для навигации
)

/** Два мероприятия, пересекающихся во времени в одном помещении */
data class OverlapPair(
    val placeId: Int,
    val placeName: String,
    val eventA: String, val titleA: String,
    val eventB: String, val titleB: String
)

/** Тип мероприятия + средняя заполненность */
data class TypeFillData(val type: String, val count: Int, val avgFill: Float, val color: Long)

/** Ячейка тепловой карты (день недели × час) */
data class HeatCell(val day: Int, val hour: Int, val count: Int)

// ── Итоговый дата-класс ───────────────────────────────────────────

data class TagStat(val tag: String, val count: Int)

data class StatisticsData(
    // Обзор
    val total: Int,
    val upcoming: Int,
    val finished: Int,
    val withRegistration: Int,
    val withoutRegistration: Int,
    val totalViews: Int,
    val totalParticipants: Int,
    val avgParticipants: Float,
    val fillRate: Float,
    val conversionRate: Float,           // participants / views * 100
    val byType: List<PieSlice>,
    val byFormat: List<PieSlice>,
    val registrationPie: List<PieSlice>,
    val topByViews: List<BarItem>,
    val topByViewsWeek: List<BarItem>,   // топ за последние 7 дней
    val topByParticipants: List<BarItem>,
    val topByFillRate: List<BarItem>,
    val viewsTimeSeries: List<ViewPoint>,
    val topEventsTimeSeries: List<Triple<String, String, List<ViewPoint>>>,

    // Типы (Раздел 2)
    val typeFillData: List<TypeFillData>,
    val byMonth: List<BarItem>,           // месяц → кол-во мероприятий

    // Локации — Пространство
    val placeStats: List<PlaceStat>,
    val locationStats: List<LocationStat>,   // по площадкам
    val placeKpd: List<PlaceKpd>,            // КПД помещений
    val idlePlaces: List<IdlePlace>,          // простаивающие
    val overlaps: List<OverlapPair>,
    // Локации — Нагрузка
    val parallelPairs: List<ParallelPair>,    // конкуренция за аудиторию
    val burdenDays: List<BurdenDay>,          // дни перегрузки
    val redundancyGroups: List<RedundancyGroup>,
    // Время (Раздел 4)
    val heatmap: List<HeatCell>,          // тепловая карта день × час
    val byDayOfWeek: List<BarItem>,       // Пн-Вс
    val byHour: List<BarItem>,            // 0..23

    // Участие (Раздел 5)
    val typeFillPie: List<PieSlice>,       // заполненность по типам

    // Тэги (Раздел 6)
    val tagStats: List<TagStat>,

    // eventId -> locationName: надёжный маппинг для UI-фильтрации в TimeTab/LocationsTab
    val eventLocationMap: Map<String, String>,

    // Аудитории, у которых 0 мероприятий за всё время
    val emptyPlaces: List<IdlePlace>
)


/** Статистика по локации (площадке целиком) */
data class LocationStat(
    val locationId: Int,
    val locationName: String,
    val placeCount: Int,       // сколько помещений в локации
    val eventCount: Int,       // всего мероприятий
    val color: Long
)

/** КПД помещения: участники / вместимость */
data class PlaceKpd(
    val placeId: Int,
    val placeName: String,
    val locationName: String,
    val capacity: Int,
    val avgParticipants: Float,  // средний реальный заполнитель
    val kpdPct: Float            // avgParticipants/capacity*100
)

/** Простаивающее помещение — нет мероприятий за 60 дней */
data class IdlePlace(
    val placeId: Int,
    val locationId: Int = 0,
    val placeName: String,
    val locationName: String,
    val capacity: Int,
    val daysSinceLastEvent: Int?  // null = никогда не использовалось
)

/** Параллельные мероприятия, конкурирующие за аудиторию */
data class ParallelPair(
    val idA: String, val titleA: String,
    val idB: String, val titleB: String,
    val overlapMinutes: Long,
    val locationA: String, val locationB: String
)

/** День с высокой организационной нагрузкой */
data class BurdenDay(
    val date: String,
    val eventCount: Int,
    val titles: List<String>
)

/** Группа избыточных мероприятий (одного типа за неделю) */
data class RedundancyGroup(
    val type: String,
    val weekLabel: String,   // "Нед. 14.04"
    val count: Int,
    val titles: List<String>
)

data class EventConversion(
    val eventId: String,
    val title: String,
    val views: Int,
    val participants: Int,
    val conversionRate: Float,
    val fillRate: Float?,
    val maxParticipants: Int?,
    val timeSeries: List<ViewPoint>   // просмотры по дням для этого события
)

// ── Период фильтрации ─────────────────────────────────────────────

enum class StatPeriod(val label: String) {
    MONTH_1("1 мес."),
    MONTHS_3("3 мес."),
    MONTHS_6("6 мес."),
    YEAR("1 год"),
    ALL("Всё время")
}

// ── ViewModel ─────────────────────────────────────────────────────

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val remoteDataSource: RemoteDataSource,
    private val refreshBus: EventRefreshBus
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _stats = MutableStateFlow<StatisticsData?>(null)
    val stats = _stats.asStateFlow()

    // Список всех мероприятий для пикера конверсии
    private val _allEvents = MutableStateFlow<List<com.events.app.domain.models.events.Event>>(emptyList())
    val allEvents = _allEvents.asStateFlow()

    // Конверсия по конкретному мероприятию
    private val _selectedEventConversion = MutableStateFlow<EventConversion?>(null)
    val selectedEventConversion = _selectedEventConversion.asStateFlow()

    private val _conversionLoading = MutableStateFlow(false)
    val conversionLoading = _conversionLoading.asStateFlow()

    // Все локации из API (включая те, к которым нет привязанных событий)
    private val _allLocationNames = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val allLocationNames = _allLocationNames.asStateFlow()

    // Оборудование выбранного помещения
    private val _placeEquipment = MutableStateFlow<List<com.events.app.data.remote.dto.EquipmentDto>>(emptyList())
    val placeEquipment = _placeEquipment.asStateFlow()

    private val _equipmentLoading = MutableStateFlow(false)
    val equipmentLoading = _equipmentLoading.asStateFlow()

    fun loadEquipmentForPlace(placeId: Int) {
        viewModelScope.launch {
            _equipmentLoading.value = true
            _placeEquipment.value = emptyList()
            try {
                var page = 1
                val result = mutableListOf<com.events.app.data.remote.dto.EquipmentDto>()
                while (true) {
                    val chunk = remoteDataSource.getEquipment(placeId = placeId, size = 30, page = page)
                    result.addAll(chunk)
                    if (chunk.size < 30) break
                    page++
                }
                _placeEquipment.value = result
            } catch (_: Exception) {
                _placeEquipment.value = emptyList()
            } finally {
                _equipmentLoading.value = false
            }
        }
    }

    fun clearPlaceEquipment() {
        _placeEquipment.value = emptyList()
    }

    // ── Период ────────────────────────────────────────────────────
    private val _selectedPeriod = MutableStateFlow(StatPeriod.ALL)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    fun selectPeriod(period: StatPeriod) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
        loadStats()
    }

    fun loadEventConversion(eventId: String) {
        viewModelScope.launch {
            _conversionLoading.value = true
            try {
                val a = remoteDataSource.getEventAnalytics(eventId)
                val event = _allEvents.value.find { it.id == eventId }
                val views = a.viewsCount ?: 0
                val participants = a.participantsCount ?: 0
                val maxP = event?.maxParticipants ?: a.maxParticipantsCount
                val rate = if (views > 0) (participants * 100f / views).coerceIn(0f, 100f) else 0f
                val fillRate = if (maxP != null && maxP > 0) (participants * 100f / maxP).coerceIn(0f, 100f) else null
                val timeSeries = a.views
                    ?.sortedBy { it.date }
                    ?.map { ViewPoint(it.date, it.views) }
                    ?: emptyList()
                _selectedEventConversion.value = EventConversion(
                    eventId = eventId,
                    title = event?.title ?: "Мероприятие",
                    views = views,
                    participants = participants,
                    conversionRate = rate,
                    fillRate = fillRate,
                    maxParticipants = maxP,
                    timeSeries = timeSeries
                )
            } catch (_: Exception) {
                _selectedEventConversion.value = null
            } finally {
                _conversionLoading.value = false
            }
        }
    }

    fun clearEventConversion() {
        _selectedEventConversion.value = null
    }

    private val palette = listOf(
        0xFF3B5BDB, 0xFF22C55E, 0xFFEF4444, 0xFFF59E0B,
        0xFF8B5CF6, 0xFF06B6D4, 0xFFEC4899, 0xFF10B981,
        0xFFFF6B35, 0xFF6366F1
    )

    init {
        loadStats()
        viewModelScope.launch { refreshBus.events.collect { loadStats() } }
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // ── Диапазон дат по выбранному периоду ───────────────────────────
                val fromDate: String? = when (_selectedPeriod.value) {
                    StatPeriod.MONTH_1  -> java.time.LocalDateTime.now().minusMonths(1).toString()
                    StatPeriod.MONTHS_3 -> java.time.LocalDateTime.now().minusMonths(3).toString()
                    StatPeriod.MONTHS_6 -> java.time.LocalDateTime.now().minusMonths(6).toString()
                    StatPeriod.YEAR     -> java.time.LocalDateTime.now().minusYears(1).toString()
                    StatPeriod.ALL      -> null
                }

                // ── Параллельно запрашиваем серверную аналитику и события ─────────
                val eventsCountDeferred  = async { try { remoteDataSource.getEventsAnalytics()     } catch (_: Exception) { null } }
                val typesAnalyticsDeferred   = async { try { remoteDataSource.getTypesAnalytics(start = fromDate)  } catch (_: Exception) { emptyList() } }
                val formatsAnalyticsDeferred = async { try { remoteDataSource.getFormatsAnalytics(start = fromDate) } catch (_: Exception) { emptyList() } }
                val locationsAnalyticsDeferred = async { try { remoteDataSource.getLocationsAnalytics(from = fromDate) } catch (_: Exception) { emptyList() } }
                val placesAnalyticsDeferred  = async { try { remoteDataSource.getPlacesAnalytics(from = fromDate)  } catch (_: Exception) { emptyList() } }
                val tagsAnalyticsDeferred    = async { try { remoteDataSource.getTagsAnalytics(from = fromDate, top = 50) } catch (_: Exception) { emptyList() } }
                // Загружаем ВСЕ события без фильтра по дате — «эта неделя» и клиентские
                // группировки должны видеть полную картину. Аналитика фильтруется сервером.
                val eventsDeferred = async { try { loadAllEvents() } catch (_: Exception) { emptyList() } }

                val events           = eventsDeferred.await()
                val eventsCountDto   = eventsCountDeferred.await()
                val typesAnalytics   = typesAnalyticsDeferred.await()
                val formatsAnalytics = formatsAnalyticsDeferred.await()
                val locationsAnalytics = locationsAnalyticsDeferred.await()
                val placesAnalytics  = placesAnalyticsDeferred.await()
                val tagsAnalytics    = tagsAnalyticsDeferred.await()

                // Параллельно обогащаем каждое событие полем needsRegistration из детального DTO
                // (ShortEventDto не содержит это поле) и загружаем аналитику за один проход.
                val enrichedAndAnalytics = events.map { event ->
                    async {
                        val detail    = try { remoteDataSource.getEventById(event.id) } catch (_: Exception) { null }
                        val analytics = try { remoteDataSource.getEventAnalytics(event.id) } catch (_: Exception) { null }
                        val enriched  = if (detail != null)
                            event.copy(needsRegistration = detail.needsRegistration ?: event.needsRegistration)
                        else event
                        Pair(enriched, Triple(event.id, event.title, analytics))
                    }
                }.awaitAll()

                val enrichedEvents   = enrichedAndAnalytics.map { it.first }
                val analyticsResults = enrichedAndAnalytics.map { it.second }

                _allEvents.value = enrichedEvents

                // ── Строим карты локаций ──────────────────────────────────────────────
                val placeToLocation   = mutableMapOf<Int, Pair<Int, String>>()
                val locationIdToName  = mutableMapOf<Int, String>()
                val eventIdToLocation = mutableMapOf<String, String>()
                // placeId → PlaceDto: для пустых аудиторий (у которых нет ни одного мероприятия)
                val placeInfoMap = mutableMapOf<Int, com.events.app.data.remote.dto.PlaceDto>()
                try {
                    val locations = remoteDataSource.getLocations()
                    locations.forEach { loc ->
                        val locName = loc.title?.takeIf { it.isNotBlank() } ?: return@forEach
                        locationIdToName[loc.id] = locName
                        try {
                            remoteDataSource.getPlacesByLocation(loc.id).forEach { place ->
                                placeToLocation[place.id] = Pair(loc.id, locName)
                                placeInfoMap[place.id] = place
                            }
                        } catch (_: Exception) {}
                        try {
                            var page = 1
                            while (true) {
                                val chunk = remoteDataSource.getEvents(
                                    locationId = loc.id, size = 30, page = page,
                                    startDateTime = fromDate
                                )
                                chunk.forEach { ev -> eventIdToLocation[ev.id] = locName }
                                if (chunk.size < 30) break
                                page++
                            }
                        } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}

                // eventsCountDto — сервер не поддерживает фильтр по дате, используем только для ALL
                val eventsCountForAggregate = if (_selectedPeriod.value == StatPeriod.ALL) eventsCountDto else null

                _stats.value = aggregate(
                    enrichedEvents, analyticsResults,
                    placeToLocation, locationIdToName, eventIdToLocation,
                    eventsCountForAggregate, typesAnalytics, formatsAnalytics,
                    locationsAnalytics, placesAnalytics, tagsAnalytics,
                    placeInfoMap
                )

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _stats.value = emptyStats()
                else _error.value = "Ошибка загрузки: ${e.code()}"
            } catch (e: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadAllEvents(): List<Event> {
        val result = mutableListOf<Event>()
        var page = 1
        while (true) {
            val chunk = try {
                getEventsUseCase(size = 30, page = page)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) break else throw e
            }
            result.addAll(chunk)
            if (chunk.size < 30) break
            page++
        }
        return result
    }

    private fun aggregate(
        events: List<Event>,
        analyticsResults: List<Triple<String, String, com.events.app.data.remote.dto.EventAnalyticDto?>>,
        placeToLocation: Map<Int, Pair<Int, String>> = emptyMap(),
        locationIdToName: Map<Int, String> = emptyMap(),
        eventIdToLocation: Map<String, String> = emptyMap(),
        eventsCountDto: com.events.app.data.remote.dto.EventsCountAnalyticsDto? = null,
        typesFromServer: List<com.events.app.data.remote.dto.TypeAnalyticsItemDto> = emptyList(),
        formatsFromServer: List<com.events.app.data.remote.dto.FormatAnalyticsItemDto> = emptyList(),
        locationsFromServer: List<com.events.app.data.remote.dto.LocationAnalyticsItemDto> = emptyList(),
        placesFromServer: List<com.events.app.data.remote.dto.PlaceAnalyticsItemDto> = emptyList(),
        tagsFromServer: List<com.events.app.data.remote.dto.TagAnalyticsItemDto> = emptyList(),
        placeInfoMap: Map<Int, com.events.app.data.remote.dto.PlaceDto> = emptyMap()
    ): StatisticsData {

        // Используем серверные счётчики если доступны, иначе считаем локально
        val total    = eventsCountDto?.totalCount    ?: events.size
        val upcoming = eventsCountDto?.upcomingCount ?: events.count { !it.isFinished }
        val finished = eventsCountDto?.finishedCount ?: events.count { it.isFinished }
        val withReg    = events.count { it.needsRegistration }
        val withoutReg = events.count { !it.needsRegistration }

        var totalViews = 0; var totalParticipants = 0
        var sumFill = 0f; var fillCount = 0

        data class Enriched(
            val id: String, val title: String,
            val views: Int, val participants: Int,
            val fillPct: Int?, val timeSeries: List<ViewsDto>,
            val type: String, val format: String,
            val startDate: LocalDateTime?, val placeId: Int?, val maxP: Int?
        )

        val enriched = analyticsResults.map { (id, title, a) ->
            val event  = events.find { it.id == id }
            val views  = a?.viewsCount ?: 0
            val part   = a?.participantsCount ?: 0
            val maxP   = event?.maxParticipants ?: a?.maxParticipantsCount
            val fillPct = if (maxP != null && maxP > 0)
                (part * 100f / maxP).roundToInt().coerceIn(0, 100) else null
            totalViews += views; totalParticipants += part
            if (fillPct != null) { sumFill += fillPct; fillCount++ }
            Enriched(id, title, views, part, fillPct, a?.views ?: emptyList(),
                event?.type ?: "", event?.format ?: "",
                event?.startDate, event?.placeId, maxP)
        }

        val convRate = if (totalViews > 0)
            (totalParticipants * 100f / totalViews).coerceIn(0f, 100f) else 0f

        // ── Временные ряды ───────────────────────────────────────
        val viewsByDate = mutableMapOf<String, Int>()
        enriched.forEach { e -> e.timeSeries.forEach { v ->
            viewsByDate[v.date] = (viewsByDate[v.date] ?: 0) + v.views
        }}
        val globalTs = viewsByDate.entries.sortedBy { it.key }
            .map { ViewPoint(it.key, it.value) }

        val topEventsTs = enriched.filter { it.timeSeries.isNotEmpty() }
            .sortedByDescending { it.views }.take(3)
            .map { e -> Triple(e.id, e.title.take(20),
                e.timeSeries.sortedBy { it.date }.map { ViewPoint(it.date, it.views) }) }

        // ── Диаграммы (Раздел 1 / Обзор) ─────────────────────────
        // Используем серверную аналитику если доступна
        val byType = if (typesFromServer.isNotEmpty()) {
            typesFromServer.sortedByDescending { it.count }.mapIndexed { i, dto ->
                PieSlice(dto.type?.ifBlank { "Не указан" } ?: "Не указан", dto.count, palette[i % palette.size])
            }
        } else {
            events.groupBy { it.type.ifBlank { "Не указан" } }
                .entries.mapIndexed { i, (l, list) ->
                    PieSlice(l, list.size, palette[i % palette.size]) }
                .sortedByDescending { it.count }
        }

        val byFormat = if (formatsFromServer.isNotEmpty()) {
            formatsFromServer.sortedByDescending { it.count }.mapIndexed { i, dto ->
                PieSlice(dto.format?.ifBlank { "Не указан" } ?: "Не указан", dto.count, palette[(i + 3) % palette.size])
            }
        } else {
            events.groupBy { it.format.ifBlank { "Не указан" } }
                .entries.mapIndexed { i, (l, list) ->
                    PieSlice(l, list.size, palette[(i + 3) % palette.size]) }
                .sortedByDescending { it.count }
        }

        val regPie = listOf(
            PieSlice("С регистрацией",  withReg,    0xFF3B5BDB),
            PieSlice("Без регистрации", withoutReg, 0xFF9E9E9E)
        ).filter { it.count > 0 }

        val topViews = enriched.filter { it.views > 0 }.sortedByDescending { it.views }
            .take(5).map { BarItem(it.title.take(26), it.views, it.id) }

        // Топ за неделю: суммируем просмотры за последние 7 дней из timeSeries
        val weekAgo = java.time.LocalDate.now().minusDays(7).toString()
        val topViewsWeek = enriched
            .map { e ->
                val weekViews = e.timeSeries
                    .filter { it.date >= weekAgo }
                    .sumOf { it.views }
                e to weekViews
            }
            .filter { (_, wv) -> wv > 0 }
            .sortedByDescending { (_, wv) -> wv }
            .take(5)
            .map { (e, wv) -> BarItem(e.title.take(26), wv, e.id) }
        val topPart  = enriched.filter { it.participants > 0 }.sortedByDescending { it.participants }
            .take(5).map { BarItem(it.title.take(26), it.participants, it.id) }
        val topFill  = enriched.filter { it.fillPct != null && it.fillPct > 0 }
            .sortedByDescending { it.fillPct }.take(5)
            .map { BarItem(it.title.take(26), it.fillPct!!, it.id) }

        // ── Типы + заполненность (Раздел 2) ──────────────────────
        val typeFillMap = enriched.groupBy { it.type.ifBlank { "Не указан" } }
        val typeFillData = typeFillMap.entries.mapIndexed { i, (type, items) ->
            val avg = items.mapNotNull { it.fillPct?.toFloat() }.let {
                if (it.isEmpty()) 0f else it.average().toFloat()
            }
            TypeFillData(type, items.size, avg, palette[i % palette.size])
        }.sortedByDescending { it.count }

        // По месяцам
        val monthMap = events.groupBy {
            it.startDate.format(java.time.format.DateTimeFormatter.ofPattern("MM.yyyy"))
        }
        val byMonth = monthMap.entries.sortedBy { it.key }
            .takeLast(12)
            .map { (month, list) -> BarItem(month, list.size) }

        // ── Локации (Раздел 3) ────────────────────────────────────
        // Группируем по placeId
        val byPlace = enriched.filter { it.placeId != null }.groupBy { it.placeId!! }

        // Статистика по помещениям — используем серверную аналитику если доступна
        val placeStats = if (placesFromServer.isNotEmpty()) {
            placesFromServer.sortedByDescending { it.count }.mapIndexed { i, dto ->
                PlaceStat(
                    placeId       = i,
                    locationId    = 0,
                    placeName     = dto.place?.ifBlank { "Помещение" } ?: "Помещение",
                    locationName  = dto.location?.ifBlank { "—" } ?: "—",
                    eventCount    = dto.count,
                    totalCapacity = 0,
                    avgFillRate   = 0f,
                    events        = emptyList()
                )
            }
        } else {
            byPlace.entries.map { (placeId, items) ->
                val event = events.find { it.placeId == placeId }
                val avgFill = items.mapNotNull { it.fillPct?.toFloat() }.let {
                    if (it.isEmpty()) 0f else it.average().toFloat()
                }
                val locPair = placeToLocation[placeId]
                PlaceStat(
                    placeId       = placeId,
                    locationId    = locPair?.first ?: 0,
                    placeName     = placeInfoMap[placeId]?.let { dto ->
                        dto.title?.takeIf { it.isNotBlank() } ?: dto.number?.let { "№$it" }
                    } ?: event?.placeTitle?.takeIf { it.isNotBlank() }
                        ?: event?.placeNumber?.let { "№$it" } ?: "Помещение $placeId",
                    locationName  = locPair?.second
                        ?: items.firstOrNull()?.id?.let { eventIdToLocation[it] }
                        ?: event?.location?.takeIf { it.isNotBlank() } ?: "—",
                    eventCount    = items.size,
                    totalCapacity = placeInfoMap[placeId]?.capacity ?: event?.placeCapacity ?: 0,
                    avgFillRate   = avgFill,
                    events        = items.map { it.id }
                )
            }.sortedByDescending { it.eventCount }
        }

        // Аудитории без единого мероприятия за всё время
        val emptyPlaces = placeInfoMap.entries
            .filter { (placeId, _) -> !byPlace.containsKey(placeId) }
            .mapNotNull { (placeId, dto) ->
                val locPair = placeToLocation[placeId] ?: return@mapNotNull null
                val name = dto.title?.takeIf { it.isNotBlank() }
                    ?: dto.number?.let { "№$it" }
                    ?: "Помещение $placeId"
                IdlePlace(placeId, locPair.first, name, locPair.second, dto.capacity, null)
            }

        // Конфликты расписания: события в одном помещении, пересекающиеся по времени
        val overlaps = mutableListOf<OverlapPair>()
        byPlace.forEach { (placeId, items) ->
            val evs = items.mapNotNull { item ->
                val ev = events.find { it.id == item.id } ?: return@mapNotNull null
                Triple(item.id, item.title, ev)
            }
            for (i in evs.indices) {
                for (j in i + 1 until evs.size) {
                    val (idA, titleA, evA) = evs[i]
                    val (idB, titleB, evB) = evs[j]
                    val startA = evA.startDate; val endA = evA.endDate
                    val startB = evB.startDate; val endB = evB.endDate
                    if (startA.isBefore(endB) && startB.isBefore(endA)) {
                        val place = evA.placeTitle?.takeIf { it.isNotBlank() }
                            ?: evA.placeNumber?.let { "№$it" } ?: "Помещение $placeId"
                        overlaps.add(OverlapPair(placeId, place, idA, titleA.take(24), idB, titleB.take(24)))
                        if (overlaps.size >= 10) return@forEach
                    }
                }
            }
        }

        // ── Пространство: locationStats — используем серверную аналитику ─
        val locationStats = if (locationsFromServer.isNotEmpty()) {
            locationsFromServer.sortedByDescending { it.count }.mapIndexed { i, dto ->
                LocationStat(i, dto.title?.ifBlank { "—" } ?: "—", 0, dto.count, palette[i % palette.size])
            }.filter { it.locationName != "—" }
        } else {
            val eventsWithLocation = events.map { ev ->
                val locName = eventIdToLocation[ev.id]
                    ?: ev.placeId?.let { placeToLocation[it]?.second }
                    ?: ev.location.takeIf { it.isNotBlank() }
                    ?: "—"
                val locId = eventIdToLocation[ev.id]?.let { name ->
                    locationIdToName.entries.firstOrNull { it.value == name }?.key
                } ?: ev.placeId?.let { placeToLocation[it]?.first } ?: 0
                Triple(ev, locName, locId)
            }
            eventsWithLocation.groupBy { (_, locName, _) -> locName }
                .entries.mapIndexed { i, (loc, evTriples) ->
                    val locId = evTriples.firstOrNull()?.third ?: i
                    LocationStat(locId, loc, evTriples.mapNotNull { it.first.placeId }.distinct().size,
                        evTriples.size, palette[i % palette.size])
                }.filter { it.locationName != "—" }.sortedByDescending { it.eventCount }
        }

        // КПД помещений
        val placeKpd = byPlace.entries.mapNotNull { (placeId, items) ->
            val ev = events.find { it.placeId == placeId } ?: return@mapNotNull null
            val cap = ev.placeCapacity ?: return@mapNotNull null
            if (cap <= 0) return@mapNotNull null
            val analyticsForPlace = analyticsResults.filter { (id, _, _) -> items.any { it.id == id } }
            val avgPart = analyticsForPlace.mapNotNull { (_, _, a) -> a?.participantsCount?.toFloat() }
                .let { if (it.isEmpty()) 0f else it.average().toFloat() }
            val kpd = (avgPart / cap * 100f).coerceIn(0f, 100f)
            val name = ev.placeTitle?.takeIf { it.isNotBlank() } ?: ev.placeNumber?.let { "№$it" } ?: "Помещение $placeId"
            PlaceKpd(placeId, name, ev.location.ifBlank { "—" }, cap, avgPart, kpd)
        }.sortedByDescending { it.kpdPct }

        // Простаивающие — нет событий за 60 дней
        val cutoff60 = java.time.LocalDateTime.now().minusDays(60)
        val idlePlaces = byPlace.entries.mapNotNull { (placeId, items) ->
            val ev = events.find { it.placeId == placeId } ?: return@mapNotNull null
            val lastEventDate = events.filter { it.placeId == placeId }.maxOfOrNull { it.startDate }
            if (lastEventDate != null && lastEventDate.isAfter(cutoff60)) return@mapNotNull null
            val daysSince = lastEventDate?.let {
                java.time.temporal.ChronoUnit.DAYS.between(it, java.time.LocalDateTime.now()).toInt()
            }
            val locPair = placeToLocation[placeId]
            val name    = placeInfoMap[placeId]?.let { dto ->
                dto.title?.takeIf { it.isNotBlank() } ?: dto.number?.let { "№$it" }
            } ?: ev.placeTitle?.takeIf { it.isNotBlank() } ?: ev.placeNumber?.let { "№$it" } ?: "Помещение $placeId"
            val locName = locPair?.second
                ?: items.firstOrNull()?.id?.let { eventIdToLocation[it] }
                ?: ev.location.ifBlank { "—" }
            IdlePlace(placeId, locPair?.first ?: 0, name, locName, placeInfoMap[placeId]?.capacity ?: ev.placeCapacity ?: 0, daysSince)
        }.sortedByDescending { it.daysSinceLastEvent ?: Int.MAX_VALUE }

        // ── Нагрузка: параллельные пары ───────────────────────────
        val parallelPairs = mutableListOf<ParallelPair>()
        val eventList = events.toList()
        for (i in eventList.indices) {
            for (j in i + 1 until eventList.size) {
                val a = eventList[i]; val b = eventList[j]
                if (a.placeId != null && b.placeId != null && a.placeId == b.placeId) continue // уже покрыто overlaps
                if (a.startDate.isBefore(b.endDate) && b.startDate.isBefore(a.endDate)) {
                    val overlapStart = maxOf(a.startDate, b.startDate)
                    val overlapEnd   = minOf(a.endDate, b.endDate)
                    val mins = java.time.temporal.ChronoUnit.MINUTES.between(overlapStart, overlapEnd)
                    if (mins >= 30) {
                        val locA = eventIdToLocation[a.id]
                            ?: a.placeId?.let { placeToLocation[it]?.second }
                            ?: a.location.ifBlank { "—" }
                        val locB = eventIdToLocation[b.id]
                            ?: b.placeId?.let { placeToLocation[it]?.second }
                            ?: b.location.ifBlank { "—" }
                        parallelPairs.add(ParallelPair(a.id, a.title.take(24), b.id, b.title.take(24),
                            mins, locA, locB))
                        if (parallelPairs.size >= 10) break
                    }
                }
            }
            if (parallelPairs.size >= 10) break
        }

        // Дни перегрузки (≥3 мероприятий в один день)
        val byDay = events.groupBy { it.startDate.toLocalDate().toString() }
        val burdenDays = byDay.entries
            .filter { (_, evs) -> evs.size >= 3 }
            .sortedByDescending { (_, evs) -> evs.size }
            .take(7)
            .map { (date, evs) -> BurdenDay(date, evs.size, evs.map { it.title.take(20) }) }

        // Избыточность типов: ≥3 одного типа за неделю
        val redundancyGroups = mutableListOf<RedundancyGroup>()
        val byWeekType = events.groupBy { ev ->
            val weekNum = ev.startDate.toLocalDate().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            val year    = ev.startDate.year
            Triple(year, weekNum, ev.type.ifBlank { "Не указан" })
        }
        byWeekType.entries.forEach { (key, evs) ->
            if (evs.size >= 3) {
                val (year, week, type) = key
                val weekLabel = "Нед. $week / $year"
                redundancyGroups.add(RedundancyGroup(type, weekLabel, evs.size, evs.map { it.title.take(20) }))
            }
        }
        val redundancySorted = redundancyGroups.sortedByDescending { it.count }.take(5)

        // ── Время (Раздел 4) ──────────────────────────────────────
        val heatmapMap = mutableMapOf<Pair<Int, Int>, Int>()
        val dayCountMap = mutableMapOf<Int, Int>()   // 1=Пн..7=Вс
        val hourCountMap = mutableMapOf<Int, Int>()

        events.forEach { ev ->
            val dow  = ev.startDate.dayOfWeek.value  // 1=Пн, 7=Вс
            val hour = ev.startDate.hour
            heatmapMap[dow to hour] = (heatmapMap[dow to hour] ?: 0) + 1
            dayCountMap[dow]  = (dayCountMap[dow] ?: 0) + 1
            hourCountMap[hour] = (hourCountMap[hour] ?: 0) + 1
        }

        val heatmap = heatmapMap.map { (k, v) -> HeatCell(k.first, k.second, v) }

        val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val byDayOfWeek = (1..7).map { d ->
            BarItem(dayNames[d - 1], dayCountMap[d] ?: 0)
        }
        val byHour = (0..23).map { h ->
            BarItem("%02d:00".format(h), hourCountMap[h] ?: 0)
        }

        // ── Участие (Раздел 5) ────────────────────────────────────
        val typeFillPie = typeFillData.filter { it.avgFill > 0 }.map { tf ->
            PieSlice(tf.type, tf.avgFill.roundToInt(), tf.color)
        }

        // ── Тэги (Раздел 6) ──────────────────────────────────────
        val tagStats = tagsFromServer
            .filter { !it.tag.isNullOrBlank() && it.count > 0 }
            .sortedByDescending { it.count }
            .map { TagStat(it.tag!!, it.count) }

        // ── EventId -> locationName маппинг (для UI-фильтрации) ─────────
        val eventLocationMap: Map<String, String> = events.associate { ev ->
            val locName = eventIdToLocation[ev.id]
                ?: ev.placeId?.let { placeToLocation[it]?.second }
                ?: ev.location.takeIf { it.isNotBlank() }
                ?: "—"
            ev.id to locName
        }

        return StatisticsData(
            total = total, upcoming = upcoming, finished = finished,
            withRegistration = withReg, withoutRegistration = withoutReg,
            totalViews = totalViews, totalParticipants = totalParticipants,
            avgParticipants = if (withReg > 0) totalParticipants.toFloat() / withReg else 0f,
            fillRate = if (fillCount > 0) sumFill / fillCount else 0f,
            conversionRate = convRate,
            byType = byType, byFormat = byFormat, registrationPie = regPie,
            topByViews = topViews, topByViewsWeek = topViewsWeek,
            topByParticipants = topPart, topByFillRate = topFill,
            viewsTimeSeries = globalTs, topEventsTimeSeries = topEventsTs,
            typeFillData = typeFillData, byMonth = byMonth,
            placeStats = placeStats, locationStats = locationStats,
            placeKpd = placeKpd, idlePlaces = idlePlaces,
            overlaps = overlaps,
            parallelPairs = parallelPairs, burdenDays = burdenDays,
            redundancyGroups = redundancySorted,
            heatmap = heatmap, byDayOfWeek = byDayOfWeek, byHour = byHour,
            typeFillPie = typeFillPie,
            tagStats = tagStats,
            eventLocationMap = eventLocationMap,
            emptyPlaces = emptyPlaces
        )
    }

    private fun emptyStats() = StatisticsData(
        total = 0, upcoming = 0, finished = 0, withRegistration = 0, withoutRegistration = 0,
        totalViews = 0, totalParticipants = 0, avgParticipants = 0f, fillRate = 0f,
        conversionRate = 0f, byType = emptyList(), byFormat = emptyList(),
        registrationPie = emptyList(), topByViews = emptyList(),
        topByViewsWeek = emptyList(),
        topByParticipants = emptyList(), topByFillRate = emptyList(),
        viewsTimeSeries = emptyList(), topEventsTimeSeries = emptyList(),
        typeFillData = emptyList(), byMonth = emptyList(),
        placeStats = emptyList(),
        locationStats = emptyList(),
        placeKpd = emptyList(),
        idlePlaces = emptyList(),
        overlaps = emptyList(),
        parallelPairs = emptyList(),
        burdenDays = emptyList(),
        redundancyGroups = emptyList(),
        heatmap = emptyList(), byDayOfWeek = emptyList(), byHour = emptyList(),
        typeFillPie = emptyList(),
        tagStats = emptyList(),
        eventLocationMap = emptyMap(),
        emptyPlaces = emptyList()
    )
}