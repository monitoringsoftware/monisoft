#!/bin/bash
#
# This is a sample start file for the MoniSoft-GUI for Linux_systems
# This file must have execute permissions to work! 
# It is using the following options:
# - 2GB RAM
# - Optimized view of images in the internal manual-PDF-file
#
# Please make sure that the path to the jmonitoring.jar file is correct
# The following path is correct if this file is in the same folder as jmonitoring.jar itself
app_path=.
 # other typical paths (sample):
# app_path=/home/moniuser/monisoft/dist
# app_path=/opt/monisoft/dist
java -Dorg.icepdf.core.scaleImages=false -jar $app_path/jmonitoring.jar
