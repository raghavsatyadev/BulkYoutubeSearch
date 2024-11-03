package io.github.raghavsatyadev.support

object Constants {

    object Other {
        const val SPLASH_COUNTER: Long = 3000

        const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
    }

    object DB {
        const val NAME: String = "BYS"
        const val VERSION = 2

        object Tables {
            const val SONG_DETAIL_TABLE = "song_detail"

            const val USER_ID = "user_id"
            const val SONG_DETAIL_ID = "id"
            const val SONG_DETAIL_OLD_DETAIL = "oldTitle"
        }
    }

    object NotificationKeys {
        const val MAIN_KEY = "main"
    }
}