package io.github.hosseinkarami_dev.floaty

import kotlin.test.Test
import kotlin.test.assertEquals

class ToastStateTest {
    @Test
    fun visibleItemsFollowFifoAndQueuedItemsPromoteAfterDismissal() {
        val state = ToastState(maxVisible = 2)
        val first = state.show(ToastMessage("first", id = "1"))
        state.show(ToastMessage("second", id = "2"))
        state.show(ToastMessage("third", id = "3"))

        assertEquals(listOf("1", "2"), state.snapshot().map { it.first })
        state.dismiss(first)
        assertEquals(listOf("2", "3"), state.snapshot().map { it.first })
    }

    @Test
    fun showingSameIdUpdatesInPlaceWithoutChangingQueueOrder() {
        val state = ToastState(maxVisible = 3)
        state.show(ToastMessage("before", id = "work"))
        state.show(ToastMessage("other", id = "other"))
        state.show(ToastMessage("after", id = "work"))

        assertEquals(listOf("work", "other"), state.snapshot().map { it.first })
        assertEquals("after", state.snapshot().first().second.title)
    }

    @Test
    fun dismissAllClearsVisibleAndQueuedNotifications() {
        val state = ToastState(maxVisible = 1)
        state.show(ToastMessage("visible"))
        state.show(ToastMessage("queued"))
        state.dismissAll()

        assertEquals(emptyList(), state.snapshot())
    }
}
