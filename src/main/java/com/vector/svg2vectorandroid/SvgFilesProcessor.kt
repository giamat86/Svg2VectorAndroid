package com.vector.svg2vectorandroid

import com.android.ide.common.vectordrawable.Svg2Vector
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

/**
 * Created by ravi on 18/12/17.
 */

internal class SvgFilesProcessor constructor(sourceSvgDirectory: String, destinationVectorDirectory: String = "$sourceSvgDirectory/ProcessedSVG", private val extension: String = "xml",
                                             private val suffix: String? = null) {

    private val sourceSvgPath: Path = Paths.get(sourceSvgDirectory)
    private val destinationVectorPath: Path = Paths.get(destinationVectorDirectory)

    fun process() {
        try {
            val options = EnumSet.of(FileVisitOption.FOLLOW_LINKS)
            //check first if source is a directory
            if (Files.isDirectory(sourceSvgPath)) {
                Files.walkFileTree(sourceSvgPath, options, Integer.MAX_VALUE, object : FileVisitor<Path> {

                    override fun postVisitDirectory(dir: Path,
                                                    exc: IOException): FileVisitResult {
                        return FileVisitResult.CONTINUE
                    }

                    override fun preVisitDirectory(dir: Path,
                                                   attrs: BasicFileAttributes): FileVisitResult {
                        // Skip folder which is processing svgs to xml
                        if (dir == destinationVectorPath) {
                            return FileVisitResult.SKIP_SUBTREE
                        }

                        val opt = arrayOf<CopyOption>(COPY_ATTRIBUTES, REPLACE_EXISTING)
                        val newDirectory = destinationVectorPath.resolve(sourceSvgPath.relativize(dir))
                        try {
                            Files.copy(dir, newDirectory, *opt)
                        } catch (ex: FileAlreadyExistsException) {
                            println("FileAlreadyExistsException " + ex.toString())
                        } catch (x: IOException) {
                            return FileVisitResult.SKIP_SUBTREE
                        }

                        return CONTINUE
                    }


                    @Throws(IOException::class)
                    override fun visitFile(file: Path,
                                           attrs: BasicFileAttributes): FileVisitResult {
                        convertToVector(file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)))
                        return CONTINUE
                    }


                    override fun visitFileFailed(file: Path,
                                                 exc: IOException): FileVisitResult {
                        return CONTINUE
                    }
                })
            } else {
                println("source not a directory")
            }

        } catch (e: IOException) {
            println("IOException " + e.message)
        }

    }

    @Throws(IOException::class)
    private fun convertToVector(source: Path, target: Path) {
        // convert only if it is .svg
        if (source.fileName.toString().endsWith(extension)) {
            val targetFile = getFileWithXMlExtension(target)
            val fous = FileOutputStream(targetFile)
            Svg2Vector.parseSvgToXml(source.toFile(), fous)
            fous.close()
        } else {
            println("Skipping file as its not svg " + source.fileName.toString())
        }
    }

    private fun getFileWithXMlExtension(target: Path): File {
        var svgFilePath = target.toFile().absolutePath
        val fileName = svgFilePath.substring(svgFilePath.lastIndexOf(File.separator))
        val newFileName = fileName.replace('-', '_')
        svgFilePath = svgFilePath.replace(fileName, newFileName)
        val svgBaseFile = StringBuilder()
        val index = svgFilePath.lastIndexOf(".")
        if (index != -1) {
            val subStr = svgFilePath.substring(0, index)
            svgBaseFile.append(subStr)
        }
        if (suffix != null)
            svgBaseFile.append(suffix)

        svgBaseFile.append(".$extension")
        return File(svgBaseFile.toString())
    }

}
