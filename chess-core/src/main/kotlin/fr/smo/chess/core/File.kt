package fr.smo.chess.core

enum class File(val value : Int, val label : String) {

    FILE_A(1,"a"),
    FILE_B(2,"b"),
    FILE_C(3,"c"),
    FILE_D(4,"d"),
    FILE_E(5,"e"),
    FILE_F(6,"f"),
    FILE_G(7,"g"),
    FILE_H(8,"h");

    companion object {
        fun at(index : Int) = entries.firstOrNull { it.value == index }
            ?: throw IllegalArgumentException("File index shall be between 1 and 8. Found: $index")

        fun at(label : String) = entries.firstOrNull { it.label == label }
            ?: throw IllegalArgumentException("File index shall be between a and h. Found: $label")
    }

    fun right() : File? = when(this) {
       FILE_A -> FILE_B
       FILE_B -> FILE_C
       FILE_C -> FILE_D
       FILE_D -> FILE_E
       FILE_E -> FILE_F
       FILE_F -> FILE_G
       FILE_G -> FILE_H
       FILE_H -> null
    }


    fun left():  File? = when(this) {
        FILE_A -> null
        FILE_B -> FILE_A
        FILE_C -> FILE_B
        FILE_D -> FILE_C
        FILE_E -> FILE_D
        FILE_F -> FILE_E
        FILE_G -> FILE_F
        FILE_H -> FILE_G
    }

}