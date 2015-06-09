# MapNodeProcess4Timestamping
## How to use it?
~~1. Put the [classifier](https://www.dropbox.com/s/k9llbp5dea4zgzp/RF5classesOnlyWithChanges.model?dl=0) into the folder (projectPath/models/).~~

1. mvn compile

2. mvn clean package -Dmaven.test.skip=true

3. use the jar in target folder as a referenced library in your code.

## Update
1. remove the predictor to accelerate the procedure (only use extractor)

2. remove the feature extraction of explicit temporal expressions. To extract features of explicit temporal expressions needs to run the NLP pipeline twice, which only add some sparse features. 

## Example

```java
package yue.temporal.MapProcess;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List; 

import yue.temporal.MapProcess.CluewebPage;
import yue.temporal.MapProcess.FeatureExtraction4MR;
import yue.temporal.MapProcess.ParagraphWithFeatures;
import yue.temporal.Utils.FileProcess;
    
public class JarTest {
	
	public static void main(String[] args) throws Exception {		
		
		//  Step 1: read clueweb 12 files and set output files
		String srcFolderPath = args[0]
		String resultFolderPath = args[1];
		File resultFolder = new File(resultFolderPath);
		if (!resultFolder.exists() || !resultFolder.isDirectory())
			resultFolder.mkdir();
		File srcFolder = new File(srcFolderPath);
		File[] srcFileList = srcFolder.listFiles();
		
		//  - Initialize the feature extractor (including the initialization of Stanford coreNLP pipeline)
		FeatureExtraction4MR featureExtractor = new FeatureExtraction4MR();		
		
		for(File srcFile: srcFileList) {					   
		    //  Step 2: content extraction -> doc info and paragraph pos
		    File resultFile = new File(resultFolder, srcFile.getName());
		    String content = FileProcess.readFile(srcFile.getAbsolutePath(), StandardCharsets.UTF_8);
		    //  - Initialize a clueweb page by reading the string of the contents (string, length threshold, similarity threshold)
		    CluewebPage cluewebPage = new CluewebPage(content, 50, 0.7);
			    
	     	//  Step 3: generate features of paragraph
		    //  - ParagraphWithFeatures include the paragraph (start position, end position, content)
		    //    and the features of the paragraph
		    List<ParagraphWithFeatures> paragraphWithFeatures = featureExtractor.extract(cluewebPage);
			    
		    //  Step 4: Output features
		    if (paragraphWithFeatures == null)
			    continue;
            for (ParagraphWithFeatures pwf: paragraphWithFeatures) {
		        FileProcess.addLinetoaFile(pwf.paragraph.getContent(), resultFile.getAbsolutePath());
			    FileProcess.addLinetoaFile(pwf.features.featuresToString(), resultFile.getAbsolutePath());
			    System.out.println(pwf.features.featuresToString());
		    }	
	    }
    }
}
```