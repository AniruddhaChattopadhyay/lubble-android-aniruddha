package `in`.lubble.app.utils

class VisibleState(val firstCompletelyVisible: Int,
                   val lastCompletelyVisible: Int) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as VisibleState
        return if (firstCompletelyVisible != that.firstCompletelyVisible) false else lastCompletelyVisible == that.lastCompletelyVisible
    }

    override fun hashCode(): Int {
        var result = firstCompletelyVisible
        result = 31 * result + lastCompletelyVisible
        return result
    }

    override fun toString(): String {
        return "VisibleState{" +
                "first=" + firstCompletelyVisible +
                ", last=" + lastCompletelyVisible +
                '}'
    }
}