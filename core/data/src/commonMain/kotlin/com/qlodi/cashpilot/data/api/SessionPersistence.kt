package com.qlodi.cashpilot.data.api

/**
 * Платформене сховище сесії. Web — localStorage (переживає перезавантаження
 * сторінки, тож залогінена людина не бачить екран входу після reload).
 * iOS — NSUserDefaults. Android — no-op (нативний застосунок не «reload»-иться,
 * тож поведінка лишається як була).
 */
expect fun saveSessionJson(json: String?)

expect fun loadSessionJson(): String?
