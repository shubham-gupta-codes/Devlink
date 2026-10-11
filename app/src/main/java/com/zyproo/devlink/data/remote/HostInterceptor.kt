package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.local.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val customHost = runBlocking { sessionManager.serverHostFlow.first() }

        if (!customHost.isNullOrBlank()) {
            val formattedHost = if (!customHost.startsWith("http://") && !customHost.startsWith("https://")) {
                "http://$customHost"
            } else {
                customHost
            }
            com.zyproo.devlink.common.ActiveHostHolder.activeHost = formattedHost
            val newUrl = formattedHost.toHttpUrlOrNull()
            if (newUrl != null) {
                val updatedUrl = request.url.newBuilder()
                    .scheme(newUrl.scheme)
                    .host(newUrl.host)
                    .port(newUrl.port)
                    .build()
                request = request.newBuilder().url(updatedUrl).build()
            }
        }
        return chain.proceed(request)
    }
}
