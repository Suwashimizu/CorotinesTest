package org.suwashizmu.coroutinestest

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * GlobalScopeではlaunchが呼ばれないのでrunBlockingを使うこと
 *
 * (ドキュメント)[https://kotlinlang.org/docs/reference/coroutines-overview.html]
 */
class ExampleUnitTest {

    private val task = SomeAsyncTask()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `add with GlobalScope`() {

        GlobalScope.launch {
            val id = task.getId()

            //printされない = 実行されない
            println("add with GlobalScope $id")

            assertEquals(10, id)
        }

    }


    @Test
    fun `add with GlobalScope with sleep`() {

        GlobalScope.launch {
            val id = task.getId()

            println("add with GlobalScope $id")

            assertEquals(100, id)
        }

        //sleepすると実行される
        Thread.sleep(500)
    }

    //runBlockingで囲むとThreadをブロックすることでコールチンの結果を受け取れる
    @Test
    fun `add with runBlocking`() = runBlocking {
        val id = task.getId()

        println("add $id")

        assertEquals(100, id)
    }

    @Test
    fun `throw from coroutines`() = runBlocking {

        //通常通りThrowされる
        try {
            val id = task.getIdWithError()
            assertEquals(100, id)
        } catch (e: Throwable) {
            assertTrue(e is IOException)
        }
    }

    @Test
    fun `should cancel by job`() {

        val job = Job()


        GlobalScope.launch(job) {
            val id = task.getId()
            //cancelによりprintされない
            println("should cancel by jon : $id")
            assertEquals(100, id)
        }

        job.cancel()
        Thread.sleep(500)
    }

    //runBlocking内でのlaunchは待機される
    @Test
    fun `in launch`() =
        runBlocking {
            // this: CoroutineScope
            launch {
                // launch new coroutine in the scope of runBlocking
                delay(1000L)
                println("World!")
            }
            println("Hello,")
        }

    @Test
    fun `in launch 2`() {
        GlobalScope.launch {
            //2Treadが生成される
            launch {
                doWorld()
            }

            launch {
                delay(500L)
                println("hello!")
            }
        }

        Thread.sleep(2000)
    }

    @Test
    fun `in launch 3`() {
        GlobalScope.launch {
            //2Treadが生成される
            launch {
                doWorld()
            }

            //ここでスレッドがブロックされるため1000ms待機
            //出力結果はworld!world!hello!となる
            doWorld()

            launch {
                delay(500L)
                println("hello!")
            }
        }

        Thread.sleep(2000)
    }

    @Test
    fun `create 100_000 coroutines`() =
        runBlocking {

            repeat(100_000) {
                // launch a lot of coroutines
                launch {
                    delay(1000L)
                    print("$it,")
                }
            }
        }

    //cancelについて
    //https://kotlinlang.org/docs/reference/coroutines/cancellation-and-timeouts.html
    @Test
    fun `Cancelling coroutine execution`() = runBlocking {
        val job = launch {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }

        }
        delay(1300L) // delay a bit

        println("main: I'm tired of waiting!")
        //cancelを呼ばなければ終了まで待つ
        //cancelを呼んでいるため即実行が終了する
        //cancelのみを呼んだ場合との違いが分からん
        job.cancel() // cancels the job
        job.join() // waits for job's completion
        println("main: Now I can quit.")
    }

    @Test
    fun `Cancellation is cooperative`() = runBlocking {

        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) { // computation loop, just wastes CPU
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        //while文は抜けていないので以下のprintが流れる
//        I'm sleeping 3 ...
//        I'm sleeping 4 ...
//        main: Now I can quit.
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }

    @Test
    fun `Making computation code cancellable`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            //自信がCancelされたかチェック
            while (isActive) { // cancellable computation loop
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }

    @Test
    fun `Closing resources with finally`() = runBlocking {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                println("I'm running finally")
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }


    @Test
    fun `Closing resources with finally while`() = runBlocking {

        val startTime = System.currentTimeMillis()
        val job = launch {
            try {
                var nextPrintTime = startTime
                var i = 0
                //このloopはdelayを使っていないため
                while (i < 5 && isActive) { // computation loop, just wastes CPU
                    // print a message twice a second
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        println("I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
                    }
                }
            } finally {
                println("I'm running finally")
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }

    @Test
    fun `Run non-cancellable block`() = runBlocking {

        val job = launch {
            try {
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                withContext(NonCancellable) {
                    println("I'm running finally")
                    delay(1000L)
                    println("And I've just delayed for 1 sec because I'm non-cancellable")
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")

    }

    @Test
    fun `Timeout`() = runBlocking {

        //TimeoutCancellationExceptionがthrowされる
        launch {
            withTimeout(11300L) {
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            }
        }

        val result = withTimeoutOrNull(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(0L)
            }
            "Done" // will get cancelled before it produces this result
        }
        //result = Done or null
        println("Result is $result")
    }

    private suspend fun doWorld() {
        delay(1000)
        println("World!")
    }
}
