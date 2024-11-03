package io.github.raghavsatyadev.support

object Constants {

    object Other {
        const val SPLASH_COUNTER: Long = 3000

        const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
    }

    object DB {
        const val NAME: String = "SundaramGold"
        const val VERSION = 2

        object Tables {
            const val VEHICLE_TABLE = "vehicle"
            const val PET_TABLE = "pet"
            const val FLAT_TABLE = "flat"
            const val FAMILY_TABLE = "family"
            const val SONG_DETAIL_TABLE = "song_detail"

            const val FLAT_ID = "id"
            const val PET_ID = "id"
            const val FAMILY_ID = "id"
            const val VEHICLE_ID = "id"
            const val SONG_DETAIL_ID = "id"
            const val SONG_DETAIL_OLD_DETAIL = "oldTitle"
        }
    }

    object NotificationKeys {
        const val MAIN_KEY = "main"
    }

    object FirebaseConstants {
        object Collections {
            const val PET = "Pet"
            const val VEHICLE = "Vehicle"
            const val FLAT = "Flat"
            const val USER = "User"
            const val FAMILY = "Family"
        }

        object Fields {
            const val IS_EMAIL_VERIFIED = "is_email_verified"
            const val NAME = "name"
            const val OCCUPATION = "occupation"
            const val SPECIALIZATION = "specialization"
            const val FLAT_ID = "flat_id"
            const val IS_PRIMARY_MEMBER = "is_primary_member"
            const val IS_PUBLIC_INFO = "is_public_info"
            const val USER_ID = "user_id"
            const val NUMBER_PLATE = "number_plate"
        }

        object Functions {
            const val SEARCH_CONTACT = "searchContact"
            const val SEARCH_VEHICLE = "searchVehicle"
        }
    }
}