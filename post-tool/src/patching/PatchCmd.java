package patching;

import java.io.File;
import java.io.IOException;

public class PatchCmd {
	String patchPath;
	int pLevel;
	
	File workingDir;
	
	public PatchCmd(String patchPath, int pLevel, File workingDir) {
		this.patchPath = patchPath;
		this.pLevel = pLevel;
		this.workingDir = workingDir;
	}
	
	public void execute() throws IOException, InterruptedException {
		String cmd[] = {"patch", "-p" + pLevel};
		
		ProcessBuilder builder = new ProcessBuilder(cmd);
		builder.redirectInput(ProcessBuilder.Redirect.from(new File(patchPath)));
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory(workingDir);

		
		Process process = builder.start();
		process.waitFor();
	}
}
