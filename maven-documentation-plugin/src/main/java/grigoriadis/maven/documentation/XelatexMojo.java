package grigoriadis.maven.documentation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Builds xelatex files for a project. The folder structure is
 * latexPath/doc1/doc1.tex. The plugin will scan all the subfolders of the
 * latexPath and compile the {folderName}.tex document.
 * 
 * @goal xelatex
 * 
 * @author grigoriadis
 */
public class XelatexMojo extends AbstractMojo {

	/**
	 * The root path of all the latex subdirectories.
	 * 
	 * @parameter default-value='src/main/resources/latex'
	 */
	private String latexPath;

	/**
	 * The path of the xelatex command.
	 * 
	 * @parameter default-value='xelatex'
	 */
	private String xelatexCommand;

	/**
	 * The output directory of the latex files.
	 * 
	 * @parameter default-value='latex'
	 */
	private String latexOutputDirectory;

	/**
	 * The extension of latex files.
	 * 
	 * @parameter default-value='.tex'
	 */
	private String latexExtension;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Running documentation plugin.");

		MavenProject project = (MavenProject) getPluginContext().get("project");

		// Check if the output folder exists, if not create it.
		File outputDirectory = new File(project.getBuild().getDirectory() + File.separator
				+ latexOutputDirectory);

		if (!outputDirectory.exists()) {
			getLog().debug(
					String.format("Directory %s not found creating new...",
							outputDirectory.getAbsolutePath()));

			outputDirectory.mkdirs();
		}

		// Check if the latex directory exists, if not skip execution.
		File latexDir = new File(latexPath);

		if (!latexDir.exists()) {
			getLog().warn(
					String.format("No subfolders found at %s skipping...",
							latexDir.getAbsolutePath()));
			return;
		}

		// Copy all the source files to the output directory
		for (File latexSubFolder : latexDir.listFiles()) {
			if (latexSubFolder.isDirectory()) {
				getLog().info(
						String.format("Copying directory %s to output", latexSubFolder.getName()));

				try {
					FileUtils.copyDirectoryToDirectory(latexSubFolder, outputDirectory);
				} catch (IOException e) {
					throw new MojoExecutionException(String.format(
							"Could not copy directory %s to %s", latexSubFolder, outputDirectory),
							e);
				}
			}
		}

		runXelatex(outputDirectory);
	}

	/**
	 * Scans the parentDirectory for subfolders and foreach subfolder it
	 * searches for a file named [subfolder].tex and runs the xelatex command.
	 * 
	 * @param parentDirectory
	 *            The parent directory
	 * @throws MojoExecutionException
	 *             In case the process failed to run
	 */
	public void runXelatex(File parentDirectory) throws MojoExecutionException {
		for (File outputLatexSubFolder : parentDirectory.listFiles()) {
			if (outputLatexSubFolder.isDirectory()) {
				File latexFile = new File(outputLatexSubFolder.getAbsolutePath() + File.separator
						+ outputLatexSubFolder.getName() + latexExtension);

				if (!latexFile.exists()) {
					throw new MojoExecutionException(String.format(
							"Directory %s must contain a file named %s",
							outputLatexSubFolder.getAbsolutePath(), latexFile.getName()));
				}

				try {
					getLog().info(
							String.format("Running latex for %s", latexFile.getAbsolutePath()));

					List<String> commands = new ArrayList<String>();
					commands.add(xelatexCommand);
					commands.add("-interaction=nonstopmode");
					commands.add(latexFile.getName());

					ProcessBuilder builder = new ProcessBuilder(commands);
					builder.directory(outputLatexSubFolder);
					Process latexProcess = builder.start();

					int outputCode = latexProcess.waitFor();

					if (outputCode != 0) {
						throw new Exception(String.format("Invalid exit code %s", outputCode));
					}
				} catch (Exception e) {
					throw new MojoExecutionException(String.format(
							"Failed to run process latex %s", latexFile.getName()), e);
				}
			}
		}
	}
}
