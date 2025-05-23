package com.firsty.bildtest.ui.haptics

import androidx.compose.runtime.Composable

enum class ReorderHapticFeedbackType {
    START,
    MOVE,
    END,
}

open class ReorderHapticFeedback {
    open fun performHapticFeedback(type: ReorderHapticFeedbackType) {
        // no-op
    }
}

@Composable
fun rememberReorderHapticFeedback(): ReorderHapticFeedback {
    return ReorderHapticFeedback()
}