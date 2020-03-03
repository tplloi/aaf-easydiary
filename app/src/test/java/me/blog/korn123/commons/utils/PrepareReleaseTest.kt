package me.blog.korn123.commons.utils

import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets

class PrepareReleaseTest {
    @Test
    @Throws(Exception::class)
    fun determine_strings_xml() {
        var defaultLanguages: List<String>? = null
        File("./src/main/res/").listFiles().map {
            if (it.name.startsWith("values")) {
                if (it.name == "values") {
                    defaultLanguages = FileUtils.readLines(File(it.absolutePath + "/strings.xml"), StandardCharsets.UTF_8)
                    defaultLanguages?.let {
                        println(it.size)
                    }

                }

                it.listFiles().map { targetFile ->
                    if (targetFile.name == "strings.xml") {
                        val lines = FileUtils.readLines(targetFile, StandardCharsets.UTF_8)
                        println(targetFile.absolutePath + ": " + lines.size)
                        lines.forEach { line ->
//                            println(line)
                        }


                    }
                }
            }
        }

        Assert.assertTrue(true)
    }
}