# setup

gradleにmoduleを追加

```
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1'

```

# Coroutine basics

https://kotlinlang.org/docs/reference/coroutines/basics.html


GlobalScope.launchはThreadと同一
GlobalScope.launch内でのdelayはThread.sleepと同一

suspend関数はGlobalScope内でしか実行出来ないよ

runBlocking{}内では現在のスレッドでCoroutineを実行できる
MainThreadで実行されるためUnitTest用と考えるべき

runBlockingは戻り値を持っているがUnitを返すのが正しい
ブロック内の最後でUnit以外の値を返している場合警告が出るので
runBlocking<Unit> {} と宣言すると警告を外すことができる
関数の末尾にUnitとか付けないように！

# Cancellation and timeouts

timeOutが便利そう
非同期処理にかかる時間を設定できる

## measureTimeMillis

実行時間を返す関数

```kotlin
measureTimeMillis {
    //doSomething
}
```

# suspendとasync関数

```kotlin
suspend fun doSomethingUsefulOne(): Int {
    delay(1000L)
            return 13
}

//Asyncを付けないと警告がでる
fun somethingUsefulOneAsync() = GlobalScope.async {
        doSomethingUsefulOne()
    }
```

どちらの書き方も可能だが推奨されるのはsuspendの方
Structured concurrency with asyncを使用する

## stat

asyncのstartにLAZYを入れると処理の実行を明示的に呼ぶことができる

```kotlin
val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
one.start()
```

# suspendでredundantが出る

withContextをつける
https://medium.com/@elizarov/blocking-threads-suspending-coroutines-d33e11bf4761

```kotlin

    suspend fun getData(): String {
        return withContext(Dispatchers.MAIN) {
            "data"
        }
    }
```

