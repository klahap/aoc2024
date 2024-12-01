package io.github.klahap

fun fileReader(name: String) = Thread.currentThread().contextClassLoader
    .getResourceAsStream(name)!!.bufferedReader()