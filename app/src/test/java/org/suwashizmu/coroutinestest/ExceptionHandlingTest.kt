package org.suwashizmu.coroutinestest

import kotlinx.coroutines.*
import org.junit.Test
import java.io.IOException

/**
 * Created by KEKE on 2019/02/25.
 */
class ExceptionHandlingTest {

    @Test
    fun `CoroutineExceptionHandler`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val job = GlobalScope.launch(handler) {
            throw AssertionError()
        }
        val deferred = GlobalScope.async(handler) {
            throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
        }

        joinAll(job, deferred)
    }

    @Test
    fun `Cancellation and exceptions`() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val job = GlobalScope.launch(handler) {
            launch {
                // the first child
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable) {
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }
            launch {
                // the second child
                delay(10)
                println("Second child throws an exception")
                throw ArithmeticException()
            }
        }
        job.join()
    }

    @Test
    fun `Exceptions aggregation`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw ArithmeticException()
                }
            }
            launch {
                delay(100)
                throw IOException()
            }
            delay(Long.MAX_VALUE)
        }
        job.join()
    }

    @Test
    fun `sequence test`() {

        println(sequence().take(10).toList())
    }

    private fun sequence() = sequence {

        var counter = 0

        while (true) {
            //処理の中断
            //C#のyieldっぽい動きをしている
            yield(counter)
            counter++
        }
    }
}