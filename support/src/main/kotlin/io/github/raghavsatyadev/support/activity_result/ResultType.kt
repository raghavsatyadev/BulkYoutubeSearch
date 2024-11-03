package io.github.raghavsatyadev.support.activity_result

enum class ResultType(val resultIndex: Int) {
    CANCELED(0),
    OK(-1),
    FIRST_USER(1);

    companion object {
        fun getEnumByResultIndex(resultIndex: Int): ResultType {
            for (resultType in entries) if (resultType.resultIndex == resultIndex) return resultType
            return CANCELED
        }

        fun size(): Int {
            return entries.size
        }
    }
}