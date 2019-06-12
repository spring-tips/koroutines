package com.example.koroutines

import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.asType
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.flow
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyFlowAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.time.Instant

@SpringBootApplication
class KoroutinesApplication

@FlowPreview
fun main() {

	fun note(msg: String) = println("${Thread.currentThread().name} : $msg ${Instant.now()}")

	fun one() {
		val job = GlobalScope.launch {
			delay(1_000)
			note("the end of one()")
		}
		note("start")
	}

	fun two() {
		note("two start")
		runBlocking {
			note("two: blocking start")
			delay(2_000)
			note("two: blocking end")
		}
		note("two stop")
	}

	fun three() {
		val deferred = (1..1_000_000).map { num ->
			GlobalScope.async {
				delay(1_000)
				num
			}
		}
		runBlocking {
			note("before sum...")
			val sum = deferred.sumBy { it.await() }
			note("after sum...: $sum")
		}
	}


	fun four() {
		val ints: Flow<Int> = flow {
			for (i in 1..10) {
				delay(1_000)
				emit(i)
			}
		}

		runBlocking {
			ints.collect { note("$it") }
		}
	}

	fun five() {

		data class Reservation(val id: Int, val name: String)

		class ReservationRepository(private val dbc: DatabaseClient) {

			suspend fun findOne(name: String): Reservation? =
					this.dbc
							.execute()
							.sql("select * from  reservation WHERE name = :name")
							.bind("name", name)
							.asType<Reservation>()
							.fetch()
							.awaitOneOrNull()

			fun all() = this.dbc.select().from("reservation").asType<Reservation>().fetch().flow()
		}

		val beans = beans {
			bean {
				ConnectionFactories.get(ref<Environment>()["spring.r2dbc.url"]!!)
			}
			bean {
				ReservationRepository(ref())
			}
			bean {
				val rr = ref<ReservationRepository>()
				coRouter {
					GET("/reservations") {
						ServerResponse.ok().bodyFlowAndAwait(rr.all())
					}
					GET("/reservations/{name}") {
						val body: Reservation = rr.findOne(it.pathVariable("name"))
								?: throw IllegalArgumentException("the name is not valid")
						ServerResponse.ok().bodyAndAwait(body)
					}
				}
			}
		}

		runApplication<KoroutinesApplication>() {
			addInitializers(beans)
		}
	}

	five()

	Thread.sleep(20_000)
//	runApplication<KoroutinesApplication>(*args)
}
