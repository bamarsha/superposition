package engine.util

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

object Resources {

    fun loadFileAsBytes(path: String): ByteArray {
        try {
            return Files.readAllBytes(Paths.get(path))
        } catch (ex: IOException) {
            System.out.println(Paths.get(path).toAbsolutePath())
            throw RuntimeException(ex)
        }

    }

    fun loadFileAsString(path: String): String {
        return String(loadFileAsBytes(path))
    }
}
