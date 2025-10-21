package com.dlight.domain.usecase.task

import com.dlight.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class CreateTaskUseCaseTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var createTaskUseCase: CreateTaskUseCase

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        createTaskUseCase = CreateTaskUseCase(taskRepository)
    }

    @Test
    fun `invoke with valid title creates task`() = runTest {
        // Given
        val title = "Test Task"
        val description = "Test Description"

        // When
        createTaskUseCase(title, description)

        // Then
        coVerify { taskRepository.createTask(any()) }
    }

    @Test
    fun `invoke with blank title throws exception`() {
        // Given
        val blankTitle = "   "

        // When/Then
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                createTaskUseCase(blankTitle)
            }
        }
    }

    @Test
    fun `invoke with empty title throws exception`() {
        // Given
        val emptyTitle = ""

        // When/Then
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                createTaskUseCase(emptyTitle)
            }
        }
    }

    @Test
    fun `invoke trims whitespace from title and description`() = runTest {
        // Given
        val titleWithSpaces = "  Test Task  "
        val descriptionWithSpaces = "  Test Description  "

        // When
        createTaskUseCase(titleWithSpaces, descriptionWithSpaces)

        // Then
        coVerify {
            taskRepository.createTask(
                match { task ->
                    task.title == "Test Task" &&
                    task.description == "Test Description"
                }
            )
        }
    }
}
