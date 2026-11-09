
package org.example.enums


enum class BookRating(val score: Int){
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    companion object{
        fun fromWord(word:String):BookRating{
            return entries.firstOrNull{it.name.equals(word, ignoreCase = true)} ?: throw IllegalArgumentException("Unknown rating: $word")
        }
    }
}

