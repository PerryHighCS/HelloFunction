package myCodeRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.regex.Matcher;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompileRequest {
	public static class FileClass {
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
		
		public String title() {
			return name.substring(0, name.lastIndexOf('.'));
		}
		
		public FileClass() {}
		
		public FileClass(String name, List<String> contents) {
			this.name = name;
			this.contents = contents;
		}
	}
	
	private int version = 1;
	private String mainClass;
	private List<FileClass> sourceFiles;
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public void setMainClass(String name) {
		mainClass = name;
	}
	
	public String getMainClass() {
		return mainClass;
	}
	
	public void setSourceFiles(FileClass[] files) {
		this.sourceFiles = new ArrayList<FileClass>(Arrays.asList(files));
	};
	
	public List<FileClass> getSourceFiles() {
		return sourceFiles;
	}
	
	
	public void addSourceFile(String name, String contents, boolean isMain) {
		if (sourceFiles == null) {
			sourceFiles = new ArrayList<FileClass>();
		}
		
		String[] contentLines = contents.split("\\r\\n|\\n|\\r");
		List<String> lines = new ArrayList<String>(Arrays.asList(contentLines));
		FileClass newFile = new FileClass(name, lines);
		
		sourceFiles.add(newFile);
		
		if(isMain) {
			mainClass = className(name, contents);
		}
	}
	
	private String className(String filename, String contents) {
		Pattern getPkgName = Pattern.compile("package\\s+(.+)\\s*;");
		Matcher matcher = getPkgName.matcher(contents);
		
		String className = filename.substring(0, filename.lastIndexOf('.'));
		
		if (matcher.find()) {
			className = matcher.group(1) + '.' + className;
		}

		return className;
	}
		
	public CompileRequest(){}
}
