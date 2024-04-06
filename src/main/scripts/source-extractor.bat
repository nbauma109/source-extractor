@echo off
SET SCRIPT_DIR=%~dp0
call "%SCRIPT_DIR%bin\java-source-extractor.bat" %*
call "%SCRIPT_DIR%bin\scala-source-extractor.bat" %*
