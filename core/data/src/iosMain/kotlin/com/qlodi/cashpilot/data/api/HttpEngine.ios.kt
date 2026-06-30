package com.qlodi.cashpilot.data.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun apiHttpClientEngine(): HttpClientEngine = Darwin.create()
