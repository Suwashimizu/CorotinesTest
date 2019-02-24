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