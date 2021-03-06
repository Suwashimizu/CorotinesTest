package org.suwashizmu.coroutinestest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Created by KEKE on 2019/02/18.
 *
 * 非同期で色々するクラス
 */
class SomeAsyncTask {
    suspend fun getId(): Int {
        delay(500)
        return 100
    }

    suspend fun getIdWithError(): Int {

        return withContext(Dispatchers.Default) {

            throw IOException("error")
            1
        }
    }

    suspend fun getData(): String {
        return withContext(Dispatchers.Default) {
            "data"
        }
    }
}