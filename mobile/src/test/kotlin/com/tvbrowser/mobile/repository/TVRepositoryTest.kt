package com.tvbrowser.mobile.repository

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.mobile.data.dao.BrowsingHistoryDao
import com.tvbrowser.mobile.data.dao.PairedTVDao
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import com.tvbrowser.mobile.data.entity.PairedTV
import com.tvbrowser.mobile.data.repository.TVRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TVRepositoryTest {

    @Mock
    private lateinit var pairedTVDao: PairedTVDao

    @Mock
    private lateinit var browsingHistoryDao: BrowsingHistoryDao

    private lateinit var repository: TVRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = TVRepository(pairedTVDao, browsingHistoryDao)
    }

    @Test
    fun `addPairedTV should insert TV into database`() = runTest {
        val tv = PairedTV(
            deviceId = "test-id",
            deviceName = "Test TV",
            ipAddress = "192.168.1.100",
            port = 8888,
            pin = "1234"
        )

        repository.addPairedTV(tv)

        verify(pairedTVDao).insert(tv)
    }

    @Test
    fun `getAllPairedTVs should return flow from DAO`() = runTest {
        val tvList = listOf(
            PairedTV("id1", "TV 1", "192.168.1.100", 8888),
            PairedTV("id2", "TV 2", "192.168.1.101", 8888)
        )
        `when`(pairedTVDao.getAllPairedTVs()).thenReturn(flowOf(tvList))

        val result = repository.getAllPairedTVs().first()

        assertEquals(2, result.size)
        assertEquals("TV 1", result[0].deviceName)
        assertEquals("TV 2", result[1].deviceName)
    }

    @Test
    fun `addHistory should insert history into database`() = runTest {
        val history = BrowsingHistory(
            url = "https://www.youtube.com",
            action = "open_url"
        )

        repository.addHistory(history)

        verify(browsingHistoryDao).insert(history)
    }

    @Test
    fun `getRecentHistory should return flow from DAO`() = runTest {
        val historyList = listOf(
            BrowsingHistory(1, "https://www.youtube.com", action = "open_url"),
            BrowsingHistory(2, "https://www.netflix.com", action = "open_url")
        )
        `when`(browsingHistoryDao.getRecentHistory(20)).thenReturn(flowOf(historyList))

        val result = repository.getRecentHistory(20).first()

        assertEquals(2, result.size)
        assertEquals("https://www.youtube.com", result[0].url)
    }

    @Test
    fun `clearHistory should delete all history from database`() = runTest {
        repository.clearHistory()

        verify(browsingHistoryDao).clearAll()
    }

    @Test
    fun `removePairedTV should delete TV from database`() = runTest {
        val tv = PairedTV("id1", "TV 1", "192.168.1.100", 8888)

        repository.removePairedTV(tv)

        verify(pairedTVDao).delete(tv)
    }
}
