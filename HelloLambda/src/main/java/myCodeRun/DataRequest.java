package myCodeRun;

import java.util.Arrays;
import java.util.List;

public class DataRequest {
	public static class DataFile {
		private String name;
		private List<String> contents;
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
				
		public void setContents(String[] contents) {
			this.contents = Arrays.asList(contents);
		}
		
		public List<String> getContents() {
			return contents;
		}
		
		public DataFile() {}
		
		public DataFile(String name, List<String> contents) {
			this.name = name;
			this.contents = contents;
		}
	}
	
	private int version = 1;
	private List<DataFile> dataFiles;
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public List<DataFile> getDataFiles() {
		return dataFiles;
	}
	public void setDataFiles(List<DataFile> dataFiles) {
		this.dataFiles = dataFiles;
	}
	
}
