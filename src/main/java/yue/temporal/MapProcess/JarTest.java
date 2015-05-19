package yue.temporal.MapProcess;
import java.io.File;
import java.util.List;

import yue.temporal.MapProcess.FeatureExtraction4MR;
import yue.temporal.MapProcess.ParagraphPrediction;
import yue.temporal.Utils.FileProcess;


public class JarTest {

	public static void main(String[] args) throws Exception {
		
		//	read clueweb 12 folder
		String srcFolderPath = args[0];
		
		//	state an output folder for the result
		String resultFolderPath = args[1];
		File resultFolder = new File(resultFolderPath);
		if (!resultFolder.exists() || !resultFolder.isDirectory())
			resultFolder.mkdir();
		
		//	initialize the function class
		FeatureExtraction4MR featureExtractor = new FeatureExtraction4MR();
		
		//	test the clueweb files one by one
		File srcFolder = new File(srcFolderPath);
		File[] srcFileList = srcFolder.listFiles();
		for(File srcFile: srcFileList) {		
			
			//	state the output file for the result	
			File resultFile = new File(resultFolder, srcFile.getName());
			
			//	generate the result based on the 5 class model which is included in the jar 
			List<ParagraphPrediction> predictionResult = featureExtractor.timePredictor(srcFile);
			
			//	output the result
			if (predictionResult == null) {				
				FileProcess.addLinetoaFile("null", resultFile.getAbsolutePath());
				System.out.println("NULL :" + srcFile.getName());
				continue;
			}
			for (ParagraphPrediction pp: predictionResult) {
				/*	pp.toString() = pp.docID + "\t" + pp.timestamps + "\t" + pp.confidenceValue
				 *                + "\n" + pp.content;
				 */
				FileProcess.addLinetoaFile(pp.toString(), resultFile.getAbsolutePath());
				System.out.println(pp.toString());
			}	
		}
	}
}
