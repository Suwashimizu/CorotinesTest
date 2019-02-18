package org.suwashizmu.coroutinestest

import kotlinx.coroutines.delay

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
}