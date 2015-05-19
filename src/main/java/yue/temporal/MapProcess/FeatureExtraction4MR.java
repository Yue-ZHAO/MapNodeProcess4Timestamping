package yue.temporal.MapProcess;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.Days;

import yue.temporal.MapProcess.CluewebPage;
import yue.temporal.MapProcess.Paragraph;
import yue.temporal.MapProcess.ParagraphFeature;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.Classifier;

public class FeatureExtraction4MR {
    
	static StanfordCoreNLP pipeline;
	static FastVector attributes = new FastVector();
	static Classifier cls;
	
	public FeatureExtraction4MR () throws Exception {
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, sutime");        
		props.put("customAnnotatorClass.sutime", "edu.stanford.nlp.time.TimeAnnotator");
		props.put("sutime.rules", "sutimeRules/defs.sutime.txt, sutimeRules/english.sutime.txt");
		pipeline = new StanfordCoreNLP(props);
		
		// load model
		InputStream isModelFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/RF5classesOnlyWithChanges.model");
		cls = (Classifier) weka.core.SerializationHelper.read(isModelFile);		
		
		Attribute pageTime = new Attribute("pageTime");
		Attribute position = new Attribute("position");
		Attribute lengthAbsolute = new Attribute("lengthAbsolute");
		Attribute lengthRelative = new Attribute("lengthRelative");
		Attribute lengthDistFormerPara = new Attribute("lengthDistFormerPara");
		Attribute lengthDistAfterPara = new Attribute("lengthDistAfterPara");
		
		Attribute numSent = new Attribute("numSent");
		Attribute lenLongSent = new Attribute("lenLongSent");
		Attribute lenShortSent = new Attribute("lenShortSent");
		Attribute lenAvgSent = new Attribute("lenAvgSent");
		
		Attribute numTEs = new Attribute("numTEs");
		Attribute numTEsBefore = new Attribute("numTEsBefore");
		Attribute numOfDate = new Attribute("numOfDate");
		Attribute numOfDuration = new Attribute("numOfDuration");
		Attribute numOfTime = new Attribute("numOfTime");
		Attribute numOfSet = new Attribute("numOfSet");
		
		Attribute lenDistAvgTEs = new Attribute("lenDistAvgTEs");
		Attribute lenDistLongTEs = new Attribute("lenDistLongTEs");
		
		Attribute valEarliestTE = new Attribute("valEarliestTE");
		Attribute valLatestTE = new Attribute("valLatestTE");
		Attribute valClosestTE = new Attribute("valClosestTE");
		Attribute valSpanTE = new Attribute("valSpanTE");
		
		FastVector fvClassVal = new FastVector();
		fvClassVal.addElement("(-inf-20.5]");
		fvClassVal.addElement("(20.5-311.5]");
		fvClassVal.addElement("(311.5-973.5]");
		fvClassVal.addElement("(973.5-2183.5]");
		fvClassVal.addElement("(2183.5-inf)");
		Attribute tagRecent = new Attribute("tagRecent", fvClassVal);
		
		attributes.addElement(tagRecent);
		attributes.addElement(pageTime);
		attributes.addElement(position);
		attributes.addElement(lengthAbsolute);
		attributes.addElement(lengthRelative);
		attributes.addElement(lengthDistFormerPara);
		attributes.addElement(lengthDistAfterPara);
		attributes.addElement(numSent);
		attributes.addElement(lenLongSent);
		attributes.addElement(lenShortSent);
		attributes.addElement(lenAvgSent);
		attributes.addElement(numTEs);
		attributes.addElement(numTEsBefore);
		attributes.addElement(numOfDate);
		attributes.addElement(numOfDuration);
		attributes.addElement(numOfTime);
		attributes.addElement(numOfSet);
		attributes.addElement(lenDistAvgTEs);
		attributes.addElement(lenDistLongTEs);
		attributes.addElement(valEarliestTE);
		attributes.addElement(valLatestTE);
		attributes.addElement(valClosestTE);
		attributes.addElement(valSpanTE);
	
	}

	public static void main(String[] args) throws Exception {

		//	Step 1: read clueweb 12 files
		String srcFilePath = args[0];
		File srcFile = new File(srcFilePath);
//		if (srcFile.exists())
//			System.out.println("Exist: " + srcFile.getAbsolutePath());
//		else
//			System.out.println("Not find: " + srcFile.getAbsolutePath());
		
		FeatureExtraction4MR featureExtractor = new FeatureExtraction4MR();
		//	Step 2: content extraction -> doc info and paragraph pos
		//	Step 3: generate features of paragraph
		//	Step 4: put feature into the model -> DocID; String; Time
		List<ParagraphPrediction> predictionResult = featureExtractor.timePredictor(srcFile);
		
		//	Step 5: Output features
		for (ParagraphPrediction pp: predictionResult) {
			// implement toString()
			System.out.println(pp.toString());
		}		
	}

	public List<ParagraphPrediction> timePredictor(File srcFile) throws Exception {
		
		List<ParagraphPrediction> paragraphPredictionList = new ArrayList<ParagraphPrediction>();
		
		CluewebPage cluewebPage = new CluewebPage(srcFile, 50, 0.7);
		List<ParagraphWithFeatures> paragraphFeatureList = extract(cluewebPage);
		if (paragraphFeatureList == null)
			return null;
		
		//	Build a test dataset
		Instances dataset = new Instances("Test-dataset", attributes, 0);
		dataset.setClassIndex(0);
		
		for (ParagraphWithFeatures pwf: paragraphFeatureList) {
			ParagraphPrediction paragraphPrediction = new ParagraphPrediction();
			paragraphPrediction.docID = cluewebPage.filename_CluewebPage;
			paragraphPrediction.content = pwf.paragraph.getContent();
	        						
			/*
			 * add a row of testing data
			 */
			double[] values = new double[dataset.numAttributes()];		
			values[1] = pwf.features.pageTime;
			values[2] = pwf.features.pos;
			values[3] = pwf.features.lenAbs;
			values[4] = pwf.features.lenRlt;
			values[5] = pwf.features.lenDistFormerPara;
			values[6] = pwf.features.lenDistAfterPara;
			
			values[7] = pwf.features.numSent;
			values[8] = pwf.features.lenLongSent;
			values[9] = pwf.features.lenShortSent;
			values[10] = pwf.features.lenAvgSent;
			
			values[11] = pwf.features.numTEs;
			values[12] = pwf.features.numTEsBefore;
			values[13] = pwf.features.numOfDate;
			values[14] = pwf.features.numOfDuration;
			values[15] = pwf.features.numOfTime;
			values[16] = pwf.features.numOfSet;
			
			values[17] = pwf.features.lenDistAvgTEs;
			values[18] = pwf.features.lenDistLongTEs;
			
			values[19] = pwf.features.valEarliestTE;
			values[20] = pwf.features.valLatestTE;
			values[21] = pwf.features.valClosestTE;
			values[22] = pwf.features.valSpanTE;
			
			Instance inst = new Instance(1.0, values);
			dataset.add(inst);
					    
		    // perform your prediction
	        double clsValue = cls.classifyInstance(dataset.lastInstance());

	        paragraphPrediction.timestamps = dataset.lastInstance().classAttribute().value((int)clsValue); 

	        
	        //	get the prediction percentage or distribution
	        double[] percentage = cls.distributionForInstance(dataset.lastInstance());

	        for (int i=0; i< percentage.length; i++) {
	        	if (i == clsValue)
	        		paragraphPrediction.confidenceValue = percentage[i];
	        }
	 
			// Add to the ParagraphPrediction List
	        paragraphPredictionList.add(paragraphPrediction);
		}
		return paragraphPredictionList;
	}
	
	public List<ParagraphPrediction> timePredictor(String cluewebFileContent) throws Exception {
		
		List<ParagraphPrediction> paragraphPredictionList = new ArrayList<ParagraphPrediction>();
		
		CluewebPage cluewebPage = new CluewebPage(cluewebFileContent, 50, 0.7);
		List<ParagraphWithFeatures> paragraphFeatureList = extract(cluewebPage);
		if (paragraphFeatureList == null)
			return null;
		
		//	Build a test dataset
		Instances dataset = new Instances("Test-dataset", attributes, 0);
		dataset.setClassIndex(0);
		
		for (ParagraphWithFeatures pwf: paragraphFeatureList) {
			ParagraphPrediction paragraphPrediction = new ParagraphPrediction();
			paragraphPrediction.docID = cluewebPage.filename_CluewebPage;
			paragraphPrediction.content = pwf.paragraph.getContent();
	        						
			/*
			 * add a row of testing data
			 */
			double[] values = new double[dataset.numAttributes()];		
			values[1] = pwf.features.pageTime;
			values[2] = pwf.features.pos;
			values[3] = pwf.features.lenAbs;
			values[4] = pwf.features.lenRlt;
			values[5] = pwf.features.lenDistFormerPara;
			values[6] = pwf.features.lenDistAfterPara;
			
			values[7] = pwf.features.numSent;
			values[8] = pwf.features.lenLongSent;
			values[9] = pwf.features.lenShortSent;
			values[10] = pwf.features.lenAvgSent;
			
			values[11] = pwf.features.numTEs;
			values[12] = pwf.features.numTEsBefore;
			values[13] = pwf.features.numOfDate;
			values[14] = pwf.features.numOfDuration;
			values[15] = pwf.features.numOfTime;
			values[16] = pwf.features.numOfSet;
			
			values[17] = pwf.features.lenDistAvgTEs;
			values[18] = pwf.features.lenDistLongTEs;
			
			values[19] = pwf.features.valEarliestTE;
			values[20] = pwf.features.valLatestTE;
			values[21] = pwf.features.valClosestTE;
			values[22] = pwf.features.valSpanTE;
			
			Instance inst = new Instance(1.0, values);
			dataset.add(inst);
					    
		    // perform your prediction
	        double clsValue = cls.classifyInstance(dataset.lastInstance());

	        paragraphPrediction.timestamps = dataset.lastInstance().classAttribute().value((int)clsValue); 

	        
	        //	get the prediction percentage or distribution
	        double[] percentage = cls.distributionForInstance(dataset.lastInstance());

	        for (int i=0; i< percentage.length; i++) {
	        	if (i == clsValue)
	        		paragraphPrediction.confidenceValue = percentage[i];
	        }
	 
			// Add to the ParagraphPrediction List
	        paragraphPredictionList.add(paragraphPrediction);
		}
		return paragraphPredictionList;
	}

	public static List<ParagraphWithFeatures> extract(CluewebPage cluewebPage) {
		
		if (cluewebPage.paragraphs.isEmpty())
			return null;
		
		DateTime baseTime = new DateTime("1996-01-01");
		List<ParagraphWithFeatures> paragraphWithFeaturesList = new ArrayList<ParagraphWithFeatures>();
		
		//	Get some information about the whole page.
		int startPosTotal = -1;		//	The start position of the first paragraph in the page
		int endPosTotal = -1;		//	The end position of the last paragraph in the page
		for (Paragraph paragraph: cluewebPage.paragraphs) {
			int startPos = paragraph.getStartPoint();
			int endPos = paragraph.getEndPoint();
			if (startPosTotal == -1 || startPos < startPosTotal)
				startPosTotal = startPos;
			if (endPosTotal == -1 || endPos > endPosTotal)
				endPosTotal = endPos;
		}
		int lenthTotal = endPosTotal- startPosTotal;
		
		String pageTimestamp = cluewebPage.currentTimestamp;
		DateTime pageTime = new DateTime(pageTimestamp);
		
		int numTEsBefore = 0;	//	The number of TEs in the same page before this paragraph
				
		/* ---------------------------- *	
		 * Procedure for each paragraph *
		 * ---------------------------- */
		for (int i =0; i<cluewebPage.paragraphs.size(); i++) {
			
			Paragraph paragraph = cluewebPage.paragraphs.get(i);
			ParagraphFeature paragraphFeature = new ParagraphFeature();
						
			//	Tag: using the time stamp of the paragraph as the tag.
			String stringParagraphTimestamps = paragraph.getTimestamp();
			String[] contentParagraphTimestamps = stringParagraphTimestamps.split("-");
			String yearPara = contentParagraphTimestamps[0];
			String monthPara = contentParagraphTimestamps[1];
			String dayPara = contentParagraphTimestamps[2];
			int monthParaInt = Integer.parseInt(monthPara);
			if(monthParaInt > 12) monthPara = "12";
			if(monthParaInt < 1) monthPara = "01";
			int dayParaInt = Integer.parseInt(dayPara);
			if(dayParaInt > 31) dayPara = "31";
			if(dayParaInt < 1) dayPara = "01";
			stringParagraphTimestamps = yearPara + "-" + monthPara + "-" + dayPara;
			
			//	Features:
			paragraphFeature.pageTime = Days.daysBetween(baseTime, pageTime).getDays();
			//	For the first paragraph in the page, treat the gap between this paragraph and the former one as 0
			paragraphFeature.pos = (double)(paragraph.getStartPoint() - startPosTotal) / lenthTotal;
			paragraphFeature.lenAbs = paragraph.getContent().length();
			paragraphFeature.lenRlt = (double)paragraphFeature.lenAbs / lenthTotal;
			if (i == 0) {
				paragraphFeature.lenDistFormerPara = 0;
			} else {
				paragraphFeature.lenDistFormerPara = (double)(paragraph.getStartPoint() - cluewebPage.paragraphs.get(i-1).getEndPoint()) / paragraphFeature.lenAbs;
			}
				
			if (i == cluewebPage.paragraphs.size()-1) {
				paragraphFeature.lenDistAfterPara = 0;
			} else {
				paragraphFeature.lenDistAfterPara = (double)(cluewebPage.paragraphs.get(i+1).getStartPoint() - paragraph.getEndPoint()) / paragraphFeature.lenAbs;			
			}
			
			//	NLP for the paragraph content 
			String text = paragraph.getContent();			
			Annotation document = new Annotation(text);
			document.set(CoreAnnotations.DocDateAnnotation.class, pageTimestamp);			
			pipeline.annotate(document);
			
			//	For features about sentences
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			
			//	Consider that the sentence number is 0			
			paragraphFeature.numSent = sentences.size();
			if (paragraphFeature.numSent == 0) {				
				paragraphFeature.lenLongSent = 0;
				paragraphFeature.lenShortSent = 0;
				paragraphFeature.lenAvgSent = 0;
			} else {				
				int sentLenTotal = 0;
				int sentLenLong = 0;
				int sentLenShort = 0;
				for(CoreMap sentence: sentences) {				
					//	get the content of the sentence
					String sentContent = sentence.toString();
					int sentLength = sentContent.length();
					if (sentLength > 0) {
						if (sentLenTotal == 0) {
							sentLenLong = sentLength;
							sentLenShort = sentLength;
						} else {
							if (sentLength > sentLenLong)
								sentLenLong = sentLength;
							if (sentLength < sentLenShort)
								sentLenShort = sentLength;
						}
						sentLenTotal += sentLength;
					}				
				}
				paragraphFeature.lenLongSent = (double)sentLenLong / paragraphFeature.lenAbs;
				paragraphFeature.lenShortSent = (double)sentLenShort / paragraphFeature.lenAbs;
				paragraphFeature.lenAvgSent = (double)(sentLenTotal / paragraphFeature.numSent) / paragraphFeature.lenAbs;
				
			}
			
			//	Consider that the number of temporal expressions is 0, use default.
			List<CoreMap> timexAnnsAll = document.get(TimeAnnotations.TimexAnnotations.class);
			paragraphFeature.numTEs = timexAnnsAll.size();
			paragraphFeature.numTEsBefore = numTEsBefore;
			numTEsBefore += paragraphFeature.numTEs;
			
			if (timexAnnsAll.size() != 0) {
			
				int numOfDate = 0;
				int numOfDuration = 0;
				int numOfTime = 0;
				int numOfSet = 0;
				
				DateTime valEarliestTE = null;
				DateTime valLatestTE = null;
				DateTime valClosestTE = null;

				int lenDistLongTEs = 0;
				int formerTimeEndPos = 0;
				int lenDistTotalTEs = paragraphFeature.lenAbs;
				
				for(CoreMap timeExpression : timexAnnsAll) {
					
					//	For the features about TE type
					String typeOfTE = (timeExpression.get(TimeExpression.Annotation.class).getTemporal().getTimexType()).toString();

					if (typeOfTE.equals("DATE")) {
						numOfDate++;
					} else if (typeOfTE.equals("TIME")) {
						numOfTime++;
					} else if (typeOfTE.equals("DURATION")) {
						numOfDuration++;
					} else if (typeOfTE.equals("SET")) {
						numOfSet++;
					}
					
					//	For the features about TE value
					String dateOfTE = timeExpression.get(TimeExpression.Annotation.class).getTemporal().getTimexValue();
 					if (dateOfTE != null && dateOfTE.matches("^(\\d+)(.*)")) {

						String[] datePart = dateOfTE.split("-");

						Boolean flagTransformable = false;
						if (datePart.length == 1 && dateOfTE.matches("^[0-9]*$")) {
							flagTransformable = true;
						} else if (datePart.length == 2) {
							String year = datePart[0];
							String month = datePart[1];
							if (year.matches("^[0-9]*$")) {
								if (month.matches("^[0-9]*$") || (month.startsWith("W") && month.substring(1).matches("^[0-9]*$")))
									flagTransformable = true;
								else if (month.equals("SP")) {
									dateOfTE = year + "-03-20";	// CHUN FEN
									flagTransformable = true;
								} else if (month.equals("SU")) {
									dateOfTE = year + "-06-21"; // XIA ZHI
									flagTransformable = true;
								} else if (month.equals("FA")) {
									dateOfTE = year + "-09-23"; // QIU FEN
									flagTransformable = true;
								} else if (month.equals("WI")) {
									dateOfTE = year + "-12-21"; // DONG ZHI, CHI JIAO ZI ^_^
									flagTransformable = true;
								}
							}
						} else if (datePart.length >= 3) {
							String year = datePart[0];
							String month = datePart[1];
							String day;
							if (datePart[2].length() <= 2)
								day = datePart[2];
							else
								day = datePart[2].substring(0, 2);
							dateOfTE = year + "-" + month + "-" + day;
							if (year.matches("^[0-9]*$") && month.matches("^[0-9]*$") && day.matches("^[0-9]*$"))
								flagTransformable = true;
						}
						
						if (flagTransformable) {

							DateTime timeValue = new DateTime(dateOfTE);
							
							//	To make sure the TE is meaningful, I set a meaningful timespan, from 1900-01-01 to 2100-12-31.
							DateTime timeMinMeaningful = new DateTime("1900-01-01");
							DateTime timeMaxMeaningful = new DateTime("2100-12-31");
							if (timeValue.isAfter(timeMinMeaningful) && timeValue.isBefore(timeMaxMeaningful)) {
								if (valEarliestTE == null || timeValue.isBefore(valEarliestTE))
									valEarliestTE = timeValue;
							
								if (valLatestTE == null || timeValue.isAfter(valLatestTE))
									valLatestTE = timeValue;
							
								if (valClosestTE == null)
									valClosestTE = timeValue;
								else {
									int daysGap1 = Days.daysBetween(timeValue, pageTime).getDays();
									int daysGap2 = Days.daysBetween(valClosestTE, pageTime).getDays();
									if (Math.abs(daysGap1) < Math.abs(daysGap2))
										valClosestTE = timeValue;	
								}
							}
						}
					}
					
					//	For the features about TE distances
					List<CoreLabel> tokens = timeExpression.get(CoreAnnotations.TokensAnnotation.class);
					int startPosition = tokens.get(0).beginPosition();
					int endPostion = tokens.get(tokens.size() - 1).endPosition();
					//	If the TE is the first one, then from 0 to the start position is the distance, else distance
					lenDistTotalTEs -= (endPostion - startPosition);
					int tempLenDist = startPosition - formerTimeEndPos;
					
					if (tempLenDist > lenDistLongTEs)
						lenDistLongTEs = tempLenDist;
					
					formerTimeEndPos = endPostion;
				}
				
				paragraphFeature.numOfDate = (double)numOfDate / paragraphFeature.numTEs;
				paragraphFeature.numOfDuration = (double)numOfDuration / paragraphFeature.numTEs;
				paragraphFeature.numOfTime = (double)numOfTime / paragraphFeature.numTEs;
				paragraphFeature.numOfSet = (double)numOfSet / paragraphFeature.numTEs;
				
				if (valEarliestTE != null)
					paragraphFeature.valEarliestTE = Days.daysBetween(baseTime, valEarliestTE).getDays();
				if (valLatestTE != null)
					paragraphFeature.valLatestTE = Days.daysBetween(baseTime, valLatestTE).getDays();
				if (valLatestTE != null)
					paragraphFeature.valClosestTE = Days.daysBetween(baseTime, valClosestTE).getDays();
				if (valEarliestTE != null && valLatestTE != null)
					paragraphFeature.valSpanTE = Days.daysBetween(valEarliestTE, valLatestTE).getDays();
				
				paragraphFeature.lenDistAvgTEs = (double)(lenDistTotalTEs / (timexAnnsAll.size() + 1)) / paragraphFeature.lenAbs;
				if (lenDistLongTEs > (paragraphFeature.lenAbs - formerTimeEndPos))
					paragraphFeature.lenDistLongTEs = (double)lenDistLongTEs / paragraphFeature.lenAbs;
				else
					paragraphFeature.lenDistLongTEs = (double)(paragraphFeature.lenAbs - formerTimeEndPos) / paragraphFeature.lenAbs;
			}
			
			//	Tokens
        	for (CoreLabel token: document.get(TokensAnnotation.class)) {
        		String pos = token.get(PartOfSpeechAnnotation.class);
        		if (pos.equals("VB"))		paragraphFeature.numVerbTense[0]++;
        		else if (pos.equals("VBD"))	paragraphFeature.numVerbTense[1]++;
        		else if (pos.equals("VBG"))	paragraphFeature.numVerbTense[2]++;
        		else if (pos.equals("VBN"))	paragraphFeature.numVerbTense[3]++;
        		else if (pos.equals("VBP"))	paragraphFeature.numVerbTense[4]++;
        		else if (pos.equals("VBZ"))	paragraphFeature.numVerbTense[5]++;
        	}
			
			//	Use 1111-11-11 as the datetime to find the explicit temporal expressions
			Annotation document_forExp = new Annotation(text);
			document_forExp.set(CoreAnnotations.DocDateAnnotation.class, "1111-11-11");			
			pipeline.annotate(document_forExp);
			List<CoreMap> timexAnnsAllExp = document_forExp.get(TimeAnnotations.TimexAnnotations.class);
			if (timexAnnsAllExp.size() != 0) {
				DateTime valEarliestExpTE = null;
				DateTime valLatestExpTE = null;
				DateTime valClosestExpTE = null;
				
				for(CoreMap timeExpression : timexAnnsAllExp) {
					String dateOfTE = timeExpression.get(TimeExpression.Annotation.class).getTemporal().getTimexValue();
					if (dateOfTE != null && dateOfTE.matches("^(\\d+)(.*)")) {
						String[] datePart = dateOfTE.split("-");
						Boolean flagTransformable = false;
						if (datePart.length == 1 && dateOfTE.matches("^[0-9]*$")) {
							flagTransformable = true;
						} else if (datePart.length == 2) {
							String year = datePart[0];
							String month = datePart[1];
							if (year.matches("^[0-9]*$")) {
								if (month.matches("^[0-9]*$") || (month.startsWith("W") && month.substring(1).matches("^[0-9]*$")))
									flagTransformable = true;
								else if (month.equals("SP")) {
									dateOfTE = year + "-03-20";	// CHUN FEN
									flagTransformable = true;
								} else if (month.equals("SU")) {
									dateOfTE = year + "-06-21"; // XIA ZHI
									flagTransformable = true;
								} else if (month.equals("FA")) {
									dateOfTE = year + "-09-23"; // QIU FEN
									flagTransformable = true;
								} else if (month.equals("WI")) {
									dateOfTE = year + "-12-21"; // DONG ZHI, CHI JIAO ZI ^_^
									flagTransformable = true;
								}
							}
						} else if (datePart.length >= 3) {
							String year = datePart[0];
							String month = datePart[1];
							String day;
							if (datePart[2].length() <= 2)
								day = datePart[2];
							else
								day = datePart[2].substring(0, 2);
							dateOfTE = year + "-" + month + "-" + day;
							if (year.matches("^[0-9]*$") && month.matches("^[0-9]*$") && day.matches("^[0-9]*$"))
								flagTransformable = true;
						}
						
						if (flagTransformable) {

							DateTime timeValue = new DateTime(dateOfTE);
							
							//	To make sure the TE is meaningful, I set a meaningful timespan, from 1900-01-01 to 2100-12-31.
							DateTime timeMinMeaningful = new DateTime("1900-01-01");
							DateTime timeMaxMeaningful = new DateTime("2100-12-31");
							if (timeValue.isAfter(timeMinMeaningful) && timeValue.isBefore(timeMaxMeaningful)) {
								//	public int numYearsExpTE[] = new int[17];	//	The number of explicit temporal expressions whose years are in 1996-2012
								int year = timeValue.getYear();
								if (year >= 1996 && year <= 2012)
									paragraphFeature.numYearsExpTE[(year-1996)]++;
								
								//	Explicit temporal expressions
								if (valEarliestExpTE == null || timeValue.isBefore(valEarliestExpTE))
									valEarliestExpTE = timeValue;
							
								if (valLatestExpTE == null || timeValue.isAfter(valLatestExpTE))
									valLatestExpTE = timeValue;
							
								if (valClosestExpTE == null)
									valClosestExpTE = timeValue;
								else {
									int daysGap1 = Days.daysBetween(timeValue, pageTime).getDays();
									int daysGap2 = Days.daysBetween(valClosestExpTE, pageTime).getDays();
									if (Math.abs(daysGap1) < Math.abs(daysGap2))
										valClosestExpTE = timeValue;	
								}
							}
						}
					}					
				}
				if (valEarliestExpTE != null)
					paragraphFeature.valEarliestExpTE = Days.daysBetween(baseTime, valEarliestExpTE).getDays();
				if (valLatestExpTE != null)
					paragraphFeature.valLatestExpTE = Days.daysBetween(baseTime, valLatestExpTE).getDays();
				if (valLatestExpTE != null)
					paragraphFeature.valClosestExpTE = Days.daysBetween(baseTime, valClosestExpTE).getDays();
				if (valEarliestExpTE != null && valLatestExpTE != null)
					paragraphFeature.valSpanExpTE = Days.daysBetween(valEarliestExpTE, valLatestExpTE).getDays();				
			}
			
			ParagraphWithFeatures paragraphWithFeatures = new ParagraphWithFeatures();
			paragraphWithFeatures.paragraph = paragraph;
			paragraphWithFeatures.features = paragraphFeature;
			paragraphWithFeaturesList.add(paragraphWithFeatures);
		}
		/* ---------------------------- *	
		 * End the Procedure for each paragraph *
		 * ---------------------------- */
		return paragraphWithFeaturesList;
	}

}
