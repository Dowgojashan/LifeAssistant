package com.example.life_assistant

open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentOrNull(): T?{
        return if(hasBeenHandled){
            null
        }
        else{
            hasBeenHandled = true
            content
        }
    }
}