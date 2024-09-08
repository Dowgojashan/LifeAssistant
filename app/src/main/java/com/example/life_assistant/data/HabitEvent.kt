package com.example.life_assistant.data

class HabitEvent {
    var name: String = ""
    var day: String = ""
    var startTime: String = ""
    var endTime: String = ""

    // 必須有一個無參數構造函數
    constructor()

    constructor(name: String, day: String, startTime: String, endTime: String) {
        this.name = name
        this.day = day
        this.startTime = startTime
        this.endTime = endTime
    }
}

