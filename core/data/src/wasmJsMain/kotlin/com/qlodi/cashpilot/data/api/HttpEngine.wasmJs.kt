package com.qlodi.cashpilot.data.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun apiHttpClientEngine(): HttpClientEngine = Js.create()
