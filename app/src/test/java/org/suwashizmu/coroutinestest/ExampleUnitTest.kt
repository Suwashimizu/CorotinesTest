package org.suwashizmu.coroutinestest

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
}
