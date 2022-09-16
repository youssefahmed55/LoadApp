package com.udacity


sealed class ButtonState {
    object Loading : ButtonState()
    object Completed : ButtonState()
}