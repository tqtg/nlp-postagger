package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static List<String> readFile(String path) {
		List<String> lines = new ArrayList<>();
		
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(path));
			while((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return lines;
	}
	
	public static void writeFile(List<String> lines, String pathname) {
		try {
			File f = new File(pathname);
			f.mkdirs();
			
			FileWriter fw = new FileWriter(f);
			for (String line : lines) {
				fw.write(line + "\n");
			}
			
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String join(List<String> list) {
		String line = "";
		for (String s : list) {
			line += s + " ";
		}
		
		return line.trim();
	}
}
