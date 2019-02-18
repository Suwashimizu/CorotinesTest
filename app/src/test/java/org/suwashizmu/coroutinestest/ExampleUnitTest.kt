package org.suwashizmu.coroutinestest

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

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

            //printされない = 呼ばれない
            println("add with GlobalScope $id")

            assertEquals(10, id)
        }

    }

    @Test
    fun `add with runBlocking`() = runBlocking {

        val id = task.getId()

        println("add $id")

        assertEquals(100, id)
    }

}
