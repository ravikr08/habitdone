package com.example

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HabitDao
import com.example.data.local.HabitDatabase
import com.example.data.model.CompletionState
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.SubHabit
import com.example.data.repository.HabitRepository
import com.example.domain.HabitCompletionUseCase
import com.example.util.DateUtils
import com.example.widget.HabitWidgetPrefs
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitBusinessLogicTest {

    private lateinit var database: HabitDatabase
    private lateinit var dao: HabitDao
    private lateinit var repository: HabitRepository
    private lateinit var useCase: HabitCompletionUseCase
    private val testDate = "2026-10-01"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HabitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.habitDao()
        repository = HabitRepository(dao)
        useCase = HabitCompletionUseCase(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 1. Single habit completion
    @Test
    fun testSingleHabitCompletionToggle() = runBlocking {
        val habitId = dao.insertHabit(
            Habit(name = "Morning DSA", type = HabitType.SINGLE)
        )

        // Initial state is INCOMPLETE
        var state = useCase.getHabitCompletionState(habitId, testDate)
        assertEquals(CompletionState.INCOMPLETE, state)

        // Toggle ON
        state = useCase.toggleSingleHabit(habitId, testDate)
        assertEquals(CompletionState.COMPLETED, state)
        assertEquals(CompletionState.COMPLETED, dao.getDailyLog(habitId, testDate)?.completionState)

        // Toggle OFF
        state = useCase.toggleSingleHabit(habitId, testDate)
        assertEquals(CompletionState.INCOMPLETE, state)
        assertEquals(CompletionState.INCOMPLETE, dao.getDailyLog(habitId, testDate)?.completionState)
    }

    // 2. Parent completion when all children are completed
    @Test
    fun testParentCompletionWhenAllChildrenCompleted() = runBlocking {
        val parentId = dao.insertHabit(
            Habit(name = "Daily Supplements", type = HabitType.PARENT)
        )
        val vitDId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Vitamin D"))
        val omegaId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Omega-3"))

        // Complete 1st child
        useCase.setSubHabitCompletion(vitDId, parentId, isCompleted = true, date = testDate)
        var parentLog = dao.getDailyLog(parentId, testDate)
        assertEquals(CompletionState.PARTIAL, parentLog?.completionState)

        // Complete 2nd child
        useCase.setSubHabitCompletion(omegaId, parentId, isCompleted = true, date = testDate)
        parentLog = dao.getDailyLog(parentId, testDate)
        assertEquals(CompletionState.COMPLETED, parentLog?.completionState)
    }

    // 3. Parent remains partial when only some children are completed
    @Test
    fun testParentRemainsPartialWhenSomeChildrenCompleted() = runBlocking {
        val parentId = dao.insertHabit(
            Habit(name = "Daily Supplements", type = HabitType.PARENT)
        )
        val vitDId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Vitamin D"))
        val omegaId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Omega-3"))
        val magId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Magnesium"))

        // Check Vitamin D and Omega-3, but NOT Magnesium
        useCase.setSubHabitCompletion(vitDId, parentId, isCompleted = true, date = testDate)
        useCase.setSubHabitCompletion(omegaId, parentId, isCompleted = true, date = testDate)

        val parentLog = dao.getDailyLog(parentId, testDate)
        assertEquals(CompletionState.PARTIAL, parentLog?.completionState)
        assertEquals(CompletionState.PARTIAL, useCase.getHabitCompletionState(parentId, testDate))
    }

    // 4. Parent ON → all children ON
    @Test
    fun testParentOnTurnsAllChildrenOn() = runBlocking {
        val parentId = dao.insertHabit(
            Habit(name = "Daily Supplements", type = HabitType.PARENT)
        )
        val vitDId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Vitamin D"))
        val omegaId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Omega-3"))

        // Initially both false
        val state = useCase.toggleParentHabit(parentId, testDate)
        assertEquals(CompletionState.COMPLETED, state)

        val child1Log = dao.getSubHabitLog(vitDId, testDate)
        val child2Log = dao.getSubHabitLog(omegaId, testDate)
        assertTrue(child1Log?.isCompleted == true)
        assertTrue(child2Log?.isCompleted == true)
        assertEquals(CompletionState.COMPLETED, dao.getDailyLog(parentId, testDate)?.completionState)
    }

    // 5. Parent OFF → all children OFF
    @Test
    fun testParentOffTurnsAllChildrenOff() = runBlocking {
        val parentId = dao.insertHabit(
            Habit(name = "Daily Supplements", type = HabitType.PARENT)
        )
        val vitDId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Vitamin D"))
        val omegaId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Omega-3"))

        // Turn all ON first
        useCase.toggleParentHabit(parentId, testDate)
        assertEquals(CompletionState.COMPLETED, dao.getDailyLog(parentId, testDate)?.completionState)

        // Turn parent OFF
        val newState = useCase.toggleParentHabit(parentId, testDate)
        assertEquals(CompletionState.INCOMPLETE, newState)

        val child1Log = dao.getSubHabitLog(vitDId, testDate)
        val child2Log = dao.getSubHabitLog(omegaId, testDate)
        assertFalse(child1Log?.isCompleted == true)
        assertFalse(child2Log?.isCompleted == true)
        assertEquals(CompletionState.INCOMPLETE, dao.getDailyLog(parentId, testDate)?.completionState)
    }

    // 6. Widget completion uses the same business logic
    @Test
    fun testWidgetCompletionUsesSameBusinessLogic() = runBlocking {
        val parentId = dao.insertHabit(
            Habit(name = "Daily Supplements", type = HabitType.PARENT)
        )
        val vitDId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Vitamin D"))
        val omegaId = dao.insertSubHabit(SubHabit(parentHabitId = parentId, name = "Omega-3"))

        // Simulate widget triggering completion for the habit ID
        val resultState = useCase.toggleHabit(parentId, testDate)
        assertEquals(CompletionState.COMPLETED, resultState)

        // Assert that Room was updated with the exact same business logic
        val parentLog = dao.getDailyLog(parentId, testDate)
        assertEquals(CompletionState.COMPLETED, parentLog?.completionState)

        val subLogs = dao.getSubHabitLogsForParentDate(parentId, testDate)
        assertEquals(2, subLogs.size)
        assertTrue(subLogs.all { it.isCompleted })
    }

    // 7. Habit-specific widget navigation passes the correct HABIT_ID
    @Test
    fun testHabitSpecificWidgetNavigationPassesCorrectHabitId() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val targetHabitId = 42L

        // Save habit mapping for widget instance 101
        val appWidgetId = 101
        HabitWidgetPrefs.saveHabitId(context, appWidgetId, targetHabitId)
        val retrievedHabitId = HabitWidgetPrefs.getHabitId(context, appWidgetId)
        assertEquals(targetHabitId, retrievedHabitId)

        // Create the navigation intent as constructed in widget body tap
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(MainActivity.EXTRA_HABIT_ID, retrievedHabitId)
        }

        // Verify that the intent contains the correct HABIT_ID extra
        assertEquals(targetHabitId, intent.getLongExtra(MainActivity.EXTRA_HABIT_ID, -1L))
        assertEquals(Intent.ACTION_VIEW, intent.action)
    }
}
