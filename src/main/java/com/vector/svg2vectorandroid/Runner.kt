package com.vector.svg2vectorandroid

/**
 * Rewrited by giamat on 07/11/18.
 */
object Runner {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("ARGUMENTS: svg/path/source <xml/path/dest> <suffix>")
            return
        }

        val sourceDirectory = args[0]


        var destinationDirectory = sourceDirectory
        var suffix: String? = null

        if (args.size == 2)
            destinationDirectory = args[1]


        if (args.size == 3)
            suffix = args[2]


        val processor = SvgFilesProcessor(sourceDirectory, destinationDirectory, suffix = suffix)
        processor.process()
    }
}
