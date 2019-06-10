package koroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.math.BigInteger
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext


@SpringBootApplication
class KoroutinesApplication

/**
 *
 * Kotlin's approach to working with asynchronous code is using coroutines,
 * which is the idea of suspendable computations, i.e. the idea that a
 * function can suspend its execution at some point and resume later on.
 *
 * <OL>
 *  <li> https://medium.com/@elizarov/blocking-threads-suspending-coroutines-d33e11bf4761 </li>
 *  <li> https://medium.com/@elizarov/cold-flows-hot-channels-d74769805f9 </li>
 *  <li> https://medium.com/@elizarov/simple-design-of-kotlin-flow-4725e7398c4c </li>
 *  <li> https://medium.com/@elizarov/kotlin-flows-and-coroutines-256260fb3bdb </li>
 *  <li> https://kotlinlang.org/docs/tutorials/coroutines/async-programming.html </li>
 *  <li> https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/ </li>
 *  <li> https://spring.io/blog/2019/04/12/going-reactive-with-spring-coroutines-and-kotlin-flow </li>
 * </OL>
 */
@FlowPreview
fun main(args: Array<String>) {

	fun one() {

		GlobalScope.launch  {
			delay(1_000)
			println("hello world")
		}

		Thread.sleep(2_000)
		println("start")
	}

	fun two() {
		GlobalScope.launch {
			delay(1_000)
			println("hello world")
		}

		runBlocking {
			delay(2_000)
		}
		println("start")
	}

	fun three() {
		val atomicNumber = AtomicInteger()
		for (i in 1..1_000_000)
			GlobalScope.launch {
				atomicNumber.addAndGet(i)
			}
		println(atomicNumber.get())
	}

	fun four() {
		val deferred = (1..1_000_000).map { n ->
			GlobalScope.async {
				delay(1_000)
				n
			}
		}
		runBlocking {
			val sum = deferred.sumBy { it.await() }
			print("Sum: $sum")
		}
	}

	fun five() {
		suspend fun sleepAndMap(n: Int): Int {
			delay(1_000)
			return n
		}

		val deferred = (1..1_000_000).map { n ->
			GlobalScope.async {
				sleepAndMap(n)
			}
		}
		runBlocking {
			val sum = deferred.sumBy { it.await() }
			print("Sum: $sum")
		}
	}

	fun six() {
		val ints: Flow<Int> = flow {
			for (i in 1..10) {
				delay(100)
				emit(i)
			}
		}
		runBlocking {
			ints.collect { println("$it @ ${Date(System.currentTimeMillis()).toInstant()} ") }
		}
		Thread.sleep(10_000)
	}


	fun seven() {


		Thread.sleep(30_1000)
	}

	seven()


}