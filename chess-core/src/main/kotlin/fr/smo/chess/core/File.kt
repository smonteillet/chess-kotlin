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
        fun at(index : Int) = File.values().firstOrNull { it.value == index }
            ?: throw IllegalArgumentException("File index shall be between 1 and 8")

        fun at(label : String) = File.values().firstOrNull { it.label == label }
            ?: throw IllegalArgumentException("File index shall be between 1 and 8")
    }

    fun right(): File? = if (value + 1 <= FILE_H.value) at(value + 1) else null
    fun left():  File? = if (value - 1 >= FILE_A.value) at(value - 1) else null

}