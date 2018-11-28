package us.msu.cse.repair.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamReaderThread extends Thread {

	private BufferedReader reader;
	private List<String> output;

	private boolean isStreamExceptional;

	public StreamReaderThread(InputStream stream) {
		this.reader = new BufferedReader(new InputStreamReader(stream));
		this.output = new ArrayList<String>();
		isStreamExceptional = false;
	}

	public List<String> getOutput() throws InterruptedException {
		return this.output;
	}

	public boolean isStreamExceptional() {
		return this.isStreamExceptional;
	}

	@Override
	public void run() {
		try {
			/* Read the output from the stream. */
			String o = null;
			while ((o = this.reader.readLine()) != null) {
				output.add(o.trim());
			}
		} catch (IOException e) {
			isStreamExceptional = true;
			e.printStackTrace();
		}
	}

}
