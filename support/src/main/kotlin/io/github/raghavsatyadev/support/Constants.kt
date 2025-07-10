package io.github.raghavsatyadev.support

object Constants {
    object Other {
        const val SPLASH_COUNTER: Long = 3000

        const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"

        const val YOUTUBE_LINK = "https://www.youtube.com/watch?v="
    }

    object DB {
        const val NAME: String = "BYS"
        const val VERSION = 2

        object Tables {
            const val SONG_DETAIL_TABLE = "song_detail"

            const val SONG_DETAIL_ID = "id"
            const val SONG_DETAIL_OLD_TITLE = "oldTitle"
            const val SONG_DETAIL_LINK = "link"
        }
    }

    object FileNames {
        const val PARENT_FOLDER_NAME = "Videos"
        const val VIDEO_NAMES = "video_names.json"
        const val VIDEO_LINKS = "video_links.json"
    }
}