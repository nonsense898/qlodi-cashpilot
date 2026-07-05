package com.qlodi.cashpilot.ui.util

@JsFun("() => { const d=new Date(); return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0'); }")
private external fun jsToday(): String

actual fun todayIsoDate(): String = jsToday()
