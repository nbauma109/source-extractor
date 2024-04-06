package com.github.nbauma109.sourceextractor;

import scala.meta._
import java.io.File
import java.nio.file.{Files, Paths, Path}
import java.nio.charset.StandardCharsets

object ScalametaExtractor {
  private def extractAndSave(tree: Tree, sourcePath: Path): Unit = {
    val sourceFileName = sourcePath.getFileName.toString.replaceFirst("[.][^.]+$", "")
    val parentPath = Option(sourcePath.getParent).getOrElse(Paths.get("."))
    
    tree.collect {
      case obj: Defn.Object if obj.name.value != sourceFileName =>
        val outputPath = parentPath.resolve(obj.name.value + ".java")
        val content = "\n" * obj.pos.startLine + obj.syntax
        println("Extracting object: " + outputPath)
        Files.write(outputPath, content.getBytes)
      case cls: Defn.Class if cls.name.value != sourceFileName =>
        val outputPath = parentPath.resolve(cls.name.value + ".java")
        val content = "\n" * cls.pos.startLine + cls.syntax
        println("Extracting class: " + outputPath)
        Files.write(outputPath, content.getBytes)
    }
  }

  private def processFile(sourcePath: Path): Unit = {
    val code = Files.readString(sourcePath, StandardCharsets.UTF_8)
    code.parse[Source] match {
      case Parsed.Success(tree) =>
        extractAndSave(tree, sourcePath)
      case Parsed.Error(pos, message, _) =>
        println(s"Parsing failed at ${sourcePath.getFileName}: line ${pos.startLine}, column ${pos.startColumn}: $message")
    }
  }

  private def processDirectory(directory: Path): Unit = {
    Files.walk(directory)
      .filter(Files.isRegularFile(_))
      .filter(_.toString.endsWith(".scala"))
      .forEach(processFile)
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: <source folder>")
      return
    }

    val path = Paths.get(args(0))
    if (Files.isDirectory(path)) {
      processDirectory(path)
    } else {
      println("The path provided is not a directory.")
    }
  }
}
