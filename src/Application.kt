package com.palanka.ktor

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private var lastUserChannel: Channel<UserInfo> = Channel(Channel.UNLIMITED)

@ExperimentalCoroutinesApi
@Suppress("unused") // Referenced in application.conf
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
        post("/auth") {
            call.respond(HttpStatusCode.OK, message = "done")
            val userInfo = call.receive<UserInfo>()
            println("Got -  ${userInfo.userName}")
            lastUserChannel.send(userInfo)
        }

        webSocket("/eventAuth") {
            println("Tablet connected")
            send(Frame.Text("Hi from server"))

            while (!lastUserChannel.isClosedForSend) {
                try {
                    val value = lastUserChannel.receive()
                    println("Sent -  ${value.userName}")
                    send(Frame.Text("${value.userName} , greetings"))
                }
                catch (e:Exception){
                    println(e.printStackTrace())
                }

            }

        }

    }


}


data class UserInfo (
    val userName:String,
)
