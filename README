 Maven Extras
============
A collection of maven plugins, archetypes etc.

maven-documentation-plugin
------------------------------
This plugin generates documentation for a project. 
Currently it compiles xelatex files to the target directory.
The project must have the following structure:

*	src/main/resources/latex
  *	document1
      *		document1.tex
      *		image.pdf
  *	document2
      *		document2.tex
      *		image.pdf

The plugin will copy all the subdirectories to the target/latex folder and run the xelatex 
command for each .tex file that its name matches the subdirectory name.
