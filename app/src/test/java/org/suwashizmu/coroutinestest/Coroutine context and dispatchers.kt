package org.suwashizmu.coroutinestest

import kotlinx.coroutines.*
import org.junit.Test
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

/**
 * Created by KEKE on 2019/02/24.
 */
class CoroutineContextAndDispatchers {


    @Test
    fun `Dispatchers and threads`() {


        println("main ThreadName:${Thread.currentThread().name}")

        //DefaultDispatcher-worker-1 = Defaultと同一
        GlobalScope.launch {
            println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }

        GlobalScope.launch(Dispatchers.Unconfined) {
            println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
        }

        //DefaultDispatcher-worker-1
        GlobalScope.launch(Dispatchers.Default) {
            // will get dispatched to DefaultDispatcher
            println("Default               : I'm working in thread ${Thread.currentThread().name}")
        }
        GlobalScope.launch(newSingleThreadContext("MyOwnThread")) {
            // will get its own new thread
            println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }

        //Unconfinedではmainスレッドで実行されているのがわかる
        /*
        main ThreadName:main
        main runBlocking      : I'm working in thread DefaultDispatcher-worker-1 @coroutine#1
        Unconfined            : I'm working in thread main @coroutine#2
        Default               : I'm working in thread DefaultDispatcher-worker-1 @coroutine#3
        newSingleThreadContext: I'm working in thread MyOwnThread @coroutine#4
         */
    }

    @Test
    fun `Unconfined vs confined dispatcher`() = runBlocking<Unit> {
        launch(Dispatchers.Unconfined) {
            // not confined -- will work with main thread
            println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
            delay(500)
            println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
        }
        launch {
            // context of the parent, main runBlocking coroutine
            println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
            delay(1000)
            println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
        }
    }

    @Test
    fun `print job`() = runBlocking<Unit> {
        println("       My job is ${coroutineContext[Job]}")

        async {
            println(" Async My job is ${coroutineContext[Job]}")
        }

        launch {
            println("Launch My job is ${coroutineContext[Job]}")
        }
        /*
       My job is "coroutine#1":BlockingCoroutine{Active}@25618e91
 Async My job is "coroutine#2":DeferredCoroutine{Active}@d7b1517
Launch My job is "coroutine#3":StandaloneCoroutine{Active}@16c0663d
         */
    }

    //job1は独立したJobになるためキャンセルされない
    //job2は親がキャンセルされると伝搬されキャンセルされる
    @Test
    fun `Children of a coroutine`() = runBlocking {
        // launch a coroutine to process some kind of incoming request
        val request = launch {
            // it spawns two other jobs, one with GlobalScope
            GlobalScope.launch {
                println("job1: I run in GlobalScope and execute independently!")
                delay(1000)
                println("job1: I am not affected by cancellation of the request")
            }
            // and the other inherits the parent context
            launch {
                delay(100)
                println("job2: I am a child of the request coroutine")
                delay(1000)
                println("job2: I will not execute this line if my parent request is cancelled")
            }
        }

        delay(500)
        request.cancel() // cancel processing of the request
        delay(1000) // delay a second to see what happens
        println("main: Who has survived request cancellation?")
    }

    //joinで処理が完了するまで待つので全ての非同期が終了してから最後のprintが実行される
    @Test
    fun `Parental responsibilities`() = runBlocking {
        // launch a coroutine to process some kind of incoming request
        val request = launch {
            repeat(3) { i ->
                // launch a few children jobs
                launch {
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    println("Coroutine $i is done")
                }
            }
            println("request: I'm done and I don't explicitly join my children that are still active")
        }

        val file = File("hoge.txt")
        PrintWriter(BufferedWriter(FileWriter(file))).apply {
            println("hogeta")
            close()
        }

        println(System.getenv())
        request.join() // wait for completion of the request, including all its children
        println("Now processing of the request is complete")
    }
}