package com.liftric.apt.aptRepository.util

import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import java.io.File

abstract class FileCompressor {
    open fun compressWithGzip(file: File): File {
        return file.compressWithGzip()
    }
}

