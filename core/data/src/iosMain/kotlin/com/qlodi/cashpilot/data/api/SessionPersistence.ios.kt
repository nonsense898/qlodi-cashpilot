package com.qlodi.cashpilot.data.api

import platform.Foundation.NSUserDefaults

private const val KEY = "cashpilot.session"

actual fun saveSessionJson(json: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (json == null) defaults.removeObjectForKey(KEY) else defaults.setObject(json, KEY)
}

actual fun loadSessionJson(): String? = NSUserDefaults.standardUserDefaults.stringForKey(KEY)
