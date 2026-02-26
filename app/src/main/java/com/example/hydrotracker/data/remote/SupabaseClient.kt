package com.example.hydrotracker.data.remote

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout

private const val SUPABASE_URL = ""
private const val SUPABASE_ANON_KEY = ""

private var _supabaseClient: SupabaseClient? = null

val supabaseClient: SupabaseClient
    get() = _supabaseClient ?: error("Call initSupabaseClient(context) before using the client")

@OptIn(SupabaseInternal::class)
fun initSupabaseClient(context: Context) {
    if (_supabaseClient != null) return
    _supabaseClient = createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
        install(Auth) {
            sessionManager = SharedPrefsSessionManager(context.applicationContext)
        }
        install(Postgrest)
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 30_000L
            }
            // Retry on connection failure (handles flaky networks and cold Supabase project wake-up)
            engine {
                (this as? io.ktor.client.engine.okhttp.OkHttpConfig)?.config {
                    retryOnConnectionFailure(true)
                }
            }
        }
    }
}
