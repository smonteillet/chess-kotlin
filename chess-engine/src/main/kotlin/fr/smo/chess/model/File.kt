package fr.smo.chess.model

enum class File(val value : Int, val label : String) {

    A(1,"a"),
    B(2,"b"),
    C(3,"c"),
    D(4,"d"),
    E(5,"e"),
    F(6,"f"),
    G(7,"g"),
    H(8,"h");

    companion object {
        fun at(index : Int) = File.values().firstOrNull { it.value == index }
            ?: throw IllegalArgumentException("File index shall be between 1 and 8")

        fun at(label : String) = File.values().firstOrNull { it.label == label }
            ?: throw IllegalArgumentException("File index shall be between 1 and 8")
    }

    fun right(): File? = if (value + 1 <= H.value) at(value + 1) else null
    fun left():  File? = if (value - 1 >= A.value) at(value - 1) else null

}