@echo off
REM
REM This is a sample start file for the MoniSoft-GUI for Windows systems
REM This file must have execute permissions to work!
REM It is using the following options:
REM - Optimized view of images in the internal manual-PDF-file
REM
REM Please make sure that the path to the jmonitoring.jar file is correct
REM The following path is correct if this file is in the same folder as jmonitoring.jar itself
set app_path=.
REM other typical paths (sample):
REM app_path=C:\Dokumente und Einstellungen\monisoftuser\Desktop\dist
REM app_path=C:\Programme\MoniSoft\dist
java -Xmx1048m -Dorg.icepdf.core.scaleImages=false -jar "%app_path%\jmonitoring.jar"