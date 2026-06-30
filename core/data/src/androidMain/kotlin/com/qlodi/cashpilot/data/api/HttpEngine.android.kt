package com.qlodi.cashpilot.data.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun apiHttpClientEngine(): HttpClientEngine = OkHttp.create()
