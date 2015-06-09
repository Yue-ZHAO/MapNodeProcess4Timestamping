package yue.temporal.MapProcess;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import yue.temporal.MapProcess.CluewebPage;
import yue.temporal.MapProcess.FeatureExtraction4MR;
//	import yue.temporal.MapProcess.ParagraphPrediction;
import yue.temporal.MapProcess.ParagraphWithFeatures;
import yue.temporal.Utils.FileProcess;


public class JarTest {

	public static void main(String[] args) throws Exception {
		
		//	Step 1: read clueweb 12 files
		String srcFolderPath = args[0];
		String resultFolderPath = args[1];
		File resultFolder = new File(resultFolderPath);
		if (!resultFolder.exists() || !resultFolder.isDirectory())
			resultFolder.mkdir();
		
		File srcFolder = new File(srcFolderPath);
		File[] srcFileList = srcFolder.listFiles();
		System.out.println(srcFileList.length);
		FeatureExtraction4MR featureExtractor = new FeatureExtraction4MR();
		
		for(File srcFile: srcFileList) {		
			//	Step 2: content extraction -> doc info and paragraph pos
			//	Step 3: generate features of paragraph
			//	Step 4: put feature into the model -> DocID; String; Time
			System.out.println(srcFile.getName());
			File resultFile = new File(resultFolder, srcFile.getName());
			String content = FileProcess.readFile(srcFile.getAbsolutePath(), StandardCharsets.UTF_8);
			CluewebPage cluewebPage = new CluewebPage(content, 50, 0.7);
			List<ParagraphWithFeatures> paragraphWithFeatures = featureExtractor.extract(cluewebPage);
			//	List<ParagraphPrediction> predictionResult = featureExtractor.timePredictor(content);
			//	if (predictionResult == null)
			//		continue;
			if (paragraphWithFeatures == null)
				continue;
			//	Step 5: Output features
			for (ParagraphWithFeatures pwf: paragraphWithFeatures) {
				// implement toString()
				FileProcess.addLinetoaFile(pwf.paragraph.getContent(), resultFile.getAbsolutePath());
				FileProcess.addLinetoaFile(pwf.features.featuresToString(), resultFile.getAbsolutePath());
				System.out.println(pwf.features.featuresToString());
			}	
		}
	}
}
