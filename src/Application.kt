package com.palanka.ktor

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.auth.*
import io.ktor.gson.*
import io.ktor.features.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import java.lang.Exception
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@ExperimentalCoroutinesApi
@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }


    install(Authentication) {
    }

    install(ContentNegotiation) {
        gson {

        }
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        post("/auth") {
            call.respond(HttpStatusCode.OK, message = "done")
            val userInfo = call.receive<UserInfo>()
            println("Got -  ${userInfo.userName}")
            println("Подключено планшетов: ${connections.size}")
            connections.forEach {
                println("Sent -  ${userInfo.userName}")
                it.session.send(Frame.Text(Gson().toJson(userInfo)))
            }
        }

        webSocket("/eventAuth{tabletName}") {

            println("Tablet connected")
            val thisConnection = Connection(this, call.parameters["tabletName"])
            connections += thisConnection

            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Пользователь $thisConnection вышел")
                connections -= thisConnection
            }

        }

    }


}

class Connection(val session: DefaultWebSocketSession, userName: String?) {
    companion object {
        var lastId = AtomicInteger(0)
    }

}


data class UserInfo(
    val userName: String,
)
