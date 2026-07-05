package com.qlodi.cashpilot.ui.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun todayIsoDate(): String {
    val f = NSDateFormatter()
    f.dateFormat = "yyyy-MM-dd"
    return f.stringFromDate(NSDate())
}
