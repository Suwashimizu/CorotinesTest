package org.suwashizmu.coroutinestest

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Created by KEKE on 2019/02/24.
 *
 * [Documents](https://kotlinlang.org/docs/reference/coroutines/composing-suspending-functions.html)
 */
class ComposingSuspendingFunctions {

    private suspend fun doSomethingUsefulOne(): Int {
        delay(1000L)
        return 13
    }

    private suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L)
        return 29
    }

    @Test
    fun `sequential by default`() = runBlocking {

        //defaultは逐次実行されるため結果は約2000msとなる
        val time = measureTimeMillis {
            val one = doSomethingUsefulOne()
            val two = doSomethingUsefulTwo()

            println("The answer is ${one + two}")
        }

        println("Completed in $time ms")
    }

    //coroutinesを同時に行う
    @Test
    fun `Concurrent using async`() = runBlocking {
        val time = measureTimeMillis {
            //Deferred
            val one = async { doSomethingUsefulOne() }
            val two = async { doSomethingUsefulTwo() }


            //awaitを呼ぶことで非同期処理が開始される
            //oneとtwoは別のjobなので同時に実行される
            println("The answer is ${one.await() + two.await()}")
        }

        println("Completed in $time ms")
    }

    @Test
    fun `Lazily started async`() = runBlocking {
        val time = measureTimeMillis {
            val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
            val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
            // some computation
            //コード側に開始の制御が渡される
            one.start() // start the first one
            two.start() // start the second one
            //startを呼ばなかった場合,awaitの段階で逐次実行される
            //この場合だと処理時間は2000msかかる
            println("The answer is ${one.await() + two.await()}")
        }
        println("Completed in $time ms")
    }

    // note, that we don't have `runBlocking` to the right of `main` in this example
    @Test
    fun `Async-style functions`() {
        val time = measureTimeMillis {
            // we can initiate async actions outside of a coroutine
            val one = somethingUsefulOneAsync()
            val two = somethingUsefulTwoAsync()
            // but waiting for a result must involve either suspending or blocking.
            // here we use `runBlocking { ... }` to block the main thread while waiting for the result
            runBlocking {
                println("The answer is ${one.await() + two.await()}")
            }
        }
        println("Completed in $time ms")
    }

    private fun somethingUsefulOneAsync() = GlobalScope.async {
        doSomethingUsefulOne()
    }

    private fun somethingUsefulTwoAsync() = GlobalScope.async {
        doSomethingUsefulTwo()
    }

    @Test
    fun `Structured concurrency with async`() = runBlocking {
        val time = measureTimeMillis {
            println("The answer is ${concurrentSum()}")
        }

        println("Completed in $time ms")
    }

    @Test
    fun `Cancellation is always propagated through coroutines hierarchy`() = runBlocking<Unit> {
        try {
            failedConcurrentSum()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            println("Computation failed with ArithmeticException")
        }
    }

    //twoでThrowされた際にcancelされ、oneへ伝搬されルためoneもcancelされる
    private suspend fun failedConcurrentSum(): Int = coroutineScope {
        val one = async {
            try {
                delay(Long.MAX_VALUE) // Emulates very long computation
                42
            } finally {
                println("First child was cancelled")
            }
        }
        val two = async<Int> {
            println("Second child throws an exception")
            throw ArithmeticException()
        }
        one.await() + two.await()
    }

    //asyncはcoroutineScopeの中で定義されている
    private suspend fun concurrentSum(): Int = coroutineScope {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }

        one.await() + two.await()
    }
}