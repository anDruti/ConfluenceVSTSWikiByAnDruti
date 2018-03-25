import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Migrate {
	
	public static void justACheck() {
		String s= "asdsad ::: {.vf-progress-placeholder} dsa";
		s = s.replaceAll(":::(.)*\\{(.)*\\}", "");
		System.out.println(s);
		
	}

	public static void main(String args[]) {

		try {
			System.out.println("START");
			List<File> filesInFolder = Files.walk(Paths.get("C:\\tmp\\Confluence-space-export-092005-81.html\\EXPORT"))
					.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
			
			for (File f : filesInFolder) {
				System.out.println(f.getName() + " changing");
				String content = new String(Files.readAllBytes(Paths.get(f.getPath())));
				
				if (content.contains("(attachments")) {
					content = content.replaceAll("\\(attachments", "\\(\\.attachments");
				};
				if (content.contains("(images/icons/")) {
				   content = content.replaceAll("\\(images\\/icons\\/", "\\(\\.attachments\\/images\\/icons\\/");
				}
				if (content.contains("(download/temp/")) {
				   content = content.replaceAll("\\(download\\/temp\\/", "\\(\\.attachments\\/download\\/temp\\/");
				}
				content = content.replaceAll("(\\()([A-Za-z0-9_-]*)(.html)(\\))", "$1$2$4");
				content = content.replaceAll(":::(.)*\\{(.)*\\}", "").replaceAll(":::","");
				content = content.replaceAll("\\{\\#(.*)\\}","");
				content = content.replaceAll("(\\+\\-\\-)((.)*)(\\-\\-\\+)+","\\'\\'\\'$1$2$3");
				Files.write(Paths.get(f.getParent() + "\\output\\" + f.getName()), content.getBytes());
			}
			
			//justACheck();
			System.out.println("END");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
}
