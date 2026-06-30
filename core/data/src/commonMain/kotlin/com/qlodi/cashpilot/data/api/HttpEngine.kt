package com.qlodi.cashpilot.data.api

import io.ktor.client.engine.HttpClientEngine

/** Платформенний рушій Ktor-клієнта: android→OkHttp, ios→Darwin, wasmJs→Js. */
expect fun apiHttpClientEngine(): HttpClientEngine
