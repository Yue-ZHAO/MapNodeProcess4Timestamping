package yue.temporal.MapProcess;

import java.io.File;
//import java.io.InputStream;
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
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
//import weka.core.Attribute;
//import weka.core.FastVector;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.classifiers.Classifier;

public class FeatureExtraction4MRBackup {
    
	static StanfordCoreNLP pipeline;
//	static FastVector attributes = new FastVector();
//	static Classifier cls;
	
	public FeatureExtraction4MRBackup () {		
		/*
		Properties props = new Properties();
		//	props.put("annotators", "tokenize, ssplit, pos, lemma, ner, sutime");
		props.put("annotators", "tokenize, ssplit, pos, sutime"); 
		props.put("customAnnotatorClass.sutime", "edu.stanford.nlp.time.TimeAnnotator");
		props.put("sutime.rules", "sutimeRules/defs.sutime.txt, sutimeRules/english.sutime.txt, sutimeRules/english.holidays.sutime.txt");
		pipeline = new StanfordCoreNLP(props);
		*/
		
	    Properties props = new Properties();
	    //  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, sutime");
	    props.put("annotators", "tokenize, ssplit, pos"); 
	    //props.put("pos.model", "tagger");
	    pipeline = new StanfordCoreNLP(props);      	    
	    String[] sutimeRules = { "sutimeRules/defs.sutime.txt", "sutimeRules/english.holidays.sutime.txt", "sutimeRules/english.sutime.txt" };
	    props.setProperty("sutime.rules",
	        org.apache.commons.lang3.StringUtils.join(sutimeRules, ","));
	    props.setProperty("sutime.binders", "0");
	    pipeline.addAnnotator(new TimeAnnotator("sutime", props));
	    
	    
	    /*		
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
	*/
	}

	public static void main(String[] args) {
		//	Step 1: read clueweb 12 files
		String srcFilePath = args[0];
		File srcFile = new File(srcFilePath);
		
		FeatureExtraction4MRBackup featureExtractor = new FeatureExtraction4MRBackup();
		//	Step 2: content extraction -> doc info and paragraph pos
		CluewebPage cluewebPage = new CluewebPage(srcFile, 50, 0.7);
		System.out.println(cluewebPage.filename_CluewebPage);
		System.out.println(cluewebPage.num_Paragraphs);
		//	Step 3: generate features of paragraph
		List<ParagraphWithFeatures> paragraphFeatureList = featureExtractor.extract(cluewebPage);
		//	Output
		for (ParagraphWithFeatures paragraphWithFeatures: paragraphFeatureList) {
			System.out.println();
			System.out.println(paragraphWithFeatures.paragraph.getContent());
			System.out.println(paragraphWithFeatures.features.featuresToString());
		}
		
		
		//	Step 4: put feature into the model -> DocID; String; Time
		//	List<ParagraphPrediction> predictionResult = featureExtractor.timePredictor(srcFile);
		
		//	Step 5: Output features
		//	for (ParagraphPrediction pp: predictionResult) {
			// implement toString()
			//	System.out.println(pp.toString());
		//	}		
	}

	/*
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
	        						
			// add a row of testing data
			 
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
	        						
			//	add a row of testing data
			 
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
	} */

	public List<ParagraphWithFeatures> extract(CluewebPage cluewebPage) {		
		
		if (cluewebPage.paragraphs.isEmpty() || cluewebPage.paragraphs==null)
			return null;
		
		//	Initialize the list of (paragraph + features)
		List<ParagraphWithFeatures> paragraphWithFeaturesList = new ArrayList<ParagraphWithFeatures>();
		
		// Set the base time to 1996-01-01
		DateTime baseTime = new DateTime("1996-01-01");
				
		//	Get some global information about the page
		//	1. Start position and end position
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
		//	2. Total length of those paragraphs
		int lenthTotal = endPosTotal- startPosTotal;
		//	3. The crawled time of the page
		String pageTimestamp = cluewebPage.currentTimestamp;
		DateTime pageTime = new DateTime(pageTimestamp);
		
		int numTEsBefore = 0;	//	The number of temporal expressions in the same page before this paragraph
				
		// --------- Procedure for each paragraph --------- 
		for (int i =0; i<cluewebPage.paragraphs.size(); i++) {
			//	Read the paragraph from cluewebPage
			Paragraph paragraph = cluewebPage.paragraphs.get(i);
			//	Initialize features of a paragraph
			ParagraphFeature paragraphFeature = new ParagraphFeature();
			
			//	Set the values of features:
			paragraphFeature.orgFile = cluewebPage.filename_CluewebPage;
			//	1. pageTime: the crawled time of the clueweb page
			paragraphFeature.pageTime = Days.daysBetween(baseTime, pageTime).getDays();
			//	2. the relative start position of the paragraph
			//		- For the first paragraph in the page, treat the gap between this paragraph and the former one as 0
			paragraphFeature.pos = (double)(paragraph.getStartPoint() - startPosTotal) / lenthTotal;
			//	3. the absolute value of the length of the paragraph
			paragraphFeature.lenAbs = paragraph.getContent().length();
			//	4. the relative calue of the length og the paragraph
			paragraphFeature.lenRlt = (double)paragraphFeature.lenAbs / lenthTotal;
			//	5. the relative distance between this paragraph and the former one
			if (i == 0) {
				paragraphFeature.lenDistFormerPara = 0;
			} else {
				paragraphFeature.lenDistFormerPara = (double)(paragraph.getStartPoint() - cluewebPage.paragraphs.get(i-1).getEndPoint()) / paragraphFeature.lenAbs;
			}
			//	6. the relative distance between this paragraph and the next one
			if (i == cluewebPage.paragraphs.size()-1) {
				paragraphFeature.lenDistAfterPara = 0;
			} else {
				paragraphFeature.lenDistAfterPara = (double)(cluewebPage.paragraphs.get(i+1).getStartPoint() - paragraph.getEndPoint()) / paragraphFeature.lenAbs;			
			}
			
			//	Set settings to NLP pipeline for the paragraph content 
			String text = paragraph.getContent();
			//	System.out.println(text);
			Annotation document = new Annotation(text);
			document.set(CoreAnnotations.DocDateAnnotation.class, pageTimestamp);			
			pipeline.annotate(document);
			
			//	For annotations about sentences
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			
			//	7. The total number of sentences in the paragraph
			paragraphFeature.numSent = sentences.size();
			//	8.9.10 the longest, shortest and average relative length of sentences in the paragraph
			if (paragraphFeature.numSent == 0) {				
				paragraphFeature.lenLongSent = 0;
				paragraphFeature.lenShortSent = 0;
				paragraphFeature.lenAvgSent = 0;
			} else {
				// Set the temp-parameters
				int sentLenTotal = 0;
				int sentLenLong = 0;
				int sentLenShort = 0;
				for(CoreMap sentence: sentences) {				
					//	Get the content of the sentence
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
					}
					sentLenTotal += sentLength;
				}
				paragraphFeature.lenLongSent = (double)sentLenLong / paragraphFeature.lenAbs;
				paragraphFeature.lenShortSent = (double)sentLenShort / paragraphFeature.lenAbs;
				paragraphFeature.lenAvgSent = (double)(sentLenTotal / paragraphFeature.numSent) / paragraphFeature.lenAbs;	
			}
			
			
			List<CoreMap> timexAnnsAll = document.get(TimeAnnotations.TimexAnnotations.class);
			//	11. the number of all kinds of temporal expressions in the paragraph
			paragraphFeature.numTEs = timexAnnsAll.size();
			//	12. the number of all kinds of temporal expressions before the paragraph
			paragraphFeature.numTEsBefore = numTEsBefore;
			numTEsBefore += paragraphFeature.numTEs;			
			//	If the number of temporal expressions is 0, use default.
			if (timexAnnsAll.size() != 0) {
				// Set the initial value of parameters
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
					
					//	Count the number of types of temporal expressions in the paragraph
					String typeOfTE = (timeExpression.get(TimeExpression.Annotation.class).getTemporal().getTimexType()).toString();
					if (typeOfTE.equals("DATE")) numOfDate++;
					else if (typeOfTE.equals("TIME")) numOfTime++;
					else if (typeOfTE.equals("DURATION")) numOfDuration++;
					else if (typeOfTE.equals("SET")) numOfSet++;
					
					//	For the features about TE value
					String dateOfTE = timeExpression.get(TimeExpression.Annotation.class).getTemporal().getTimexValue();
					//	If the date is not null and start with a number
 					if (dateOfTE != null && dateOfTE.matches("^(\\d+)(.*)")) {
						String[] datePart = dateOfTE.split("-");
						Boolean flagTransformable = false;	
						if (datePart.length == 1 && dateOfTE.matches("^[0-9]*$")) {
							//	if the date is only a number, we think it is a year
							flagTransformable = true;
						} else if (datePart.length == 2) {		
							String year = datePart[0];
							String month = datePart[1];
							if (year.matches("^[0-9]*$")) {
								// (year - month) or (year - week) or (year - season)
								if (month.matches("^[0-9]*$") || (month.startsWith("W") && month.substring(1).matches("^[0-9]*$")))
									flagTransformable = true;
								else if (month.equals("SP")) {	// Spring
									dateOfTE = year + "-03-20";	// CHUN FEN
									flagTransformable = true;
								} else if (month.equals("SU")) {// Summer
									dateOfTE = year + "-06-21"; // XIA ZHI
									flagTransformable = true;
								} else if (month.equals("FA")) {// Fall
									dateOfTE = year + "-09-23"; // QIU FEN
									flagTransformable = true;
								} else if (month.equals("WI")) {// Winter
									dateOfTE = year + "-12-21"; // DONG ZHI, CHI JIAO ZI ^_^
									flagTransformable = true;
								}
							}
						} else if (datePart.length >= 3) {
							// year + month + day
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
							//	To make sure the temporal expression is meaningful, I set a meaningful time span, 
							//	from 1900-01-01 to 2100-12-31.
							DateTime timeMinMeaningful = new DateTime("1900-01-01");
							DateTime timeMaxMeaningful = new DateTime("2100-12-31");
							if (timeValue.isAfter(timeMinMeaningful) && timeValue.isBefore(timeMaxMeaningful)) {
								//	Count the number of years of temporal expressions
								int year = timeValue.getYear();
								if (year >= 1996 && year <= 2012)
									paragraphFeature.numYearsTE[(year-1996)]++;
								//	Find the earliest temporal expression in the paragraph
								if (valEarliestTE == null || timeValue.isBefore(valEarliestTE))
									valEarliestTE = timeValue;
								//	Find the latest temporal expression in the paragraph
								if (valLatestTE == null || timeValue.isAfter(valLatestTE))
									valLatestTE = timeValue;
								//	Find the closest temporal expression to the crawled time in the paragraph
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
					
					//	For the features about the length of distances between 2 temporal expressions
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
				//	13 - 16 the relative number of different types of temporal expressions
				paragraphFeature.numOfDate = (double)numOfDate / paragraphFeature.numTEs;
				paragraphFeature.numOfDuration = (double)numOfDuration / paragraphFeature.numTEs;
				paragraphFeature.numOfTime = (double)numOfTime / paragraphFeature.numTEs;
				paragraphFeature.numOfSet = (double)numOfSet / paragraphFeature.numTEs;
				//	17 - 20 the value and timespan of temporal expressions
				if (valEarliestTE != null) // else paragraphFeature.valEarliestTE = 0 
					paragraphFeature.valEarliestTE = Days.daysBetween(baseTime, valEarliestTE).getDays();
				if (valLatestTE != null)  // else paragraphFeature.valLatestTE = 0
					paragraphFeature.valLatestTE = Days.daysBetween(baseTime, valLatestTE).getDays();
				if (valLatestTE != null)  // else paragraphFeature.valClosestTE = 0
					paragraphFeature.valClosestTE = Days.daysBetween(baseTime, valClosestTE).getDays();
				if (valEarliestTE != null && valLatestTE != null)  // else paragraphFeature.valSpanTE = 0
					paragraphFeature.valSpanTE = Days.daysBetween(valEarliestTE, valLatestTE).getDays();
				//	21 the relative avg length of the distance between 2 TEs 
				paragraphFeature.lenDistAvgTEs = (double)(lenDistTotalTEs / (timexAnnsAll.size() + 1)) / paragraphFeature.lenAbs;
				//	22 the relative longest length of the distance between 2 TEs
				if (lenDistLongTEs > (paragraphFeature.lenAbs - formerTimeEndPos))
					paragraphFeature.lenDistLongTEs = (double)lenDistLongTEs / paragraphFeature.lenAbs;
				else
					paragraphFeature.lenDistLongTEs = (double)(paragraphFeature.lenAbs - formerTimeEndPos) / paragraphFeature.lenAbs;
			}
			
			
			//	23 - 28 the number of different types of verbs
			//	POS Tokens
        	for (CoreLabel token: document.get(TokensAnnotation.class)) {
        		String pos = token.get(PartOfSpeechAnnotation.class);
        		if (pos.equals("VB"))		paragraphFeature.numVerbTense[0]++;
        		else if (pos.equals("VBD"))	paragraphFeature.numVerbTense[1]++;
        		else if (pos.equals("VBG"))	paragraphFeature.numVerbTense[2]++;
        		else if (pos.equals("VBN"))	paragraphFeature.numVerbTense[3]++;
        		else if (pos.equals("VBP"))	paragraphFeature.numVerbTense[4]++;
        		else if (pos.equals("VBZ"))	paragraphFeature.numVerbTense[5]++;
        	}
			
//			//	Use 1111-11-11 as the datetime to find the explicit temporal expressions
//			Annotation document_forExp = new Annotation(text);
//			document_forExp.set(CoreAnnotations.DocDateAnnotation.class, "1111-11-11");			
//			pipeline.annotate(document_forExp);
//			List<CoreMap> timexAnnsAllExp = document_forExp.get(TimeAnnotations.TimexAnnotations.class);
//			if (timexAnnsAllExp.size() != 0) {
//				DateTime valEarliestExpTE = null;
//				DateTime valLatestExpTE = null;
//				DateTime valClosestExpTE = null;
//				
//				for(CoreMap timeExpression : timexAnnsAllExp) {
//					String dateOfTE = timeExpression.get(TimeExpression.Annotation.class).getTemporal().getTimexValue();
//					if (dateOfTE != null && dateOfTE.matches("^(\\d+)(.*)")) {
//						String[] datePart = dateOfTE.split("-");
//						Boolean flagTransformable = false;
//						if (datePart.length == 1 && dateOfTE.matches("^[0-9]*$")) flagTransformable = true;
//						else if (datePart.length == 2) {
//							String year = datePart[0];
//							String month = datePart[1];
//							if (year.matches("^[0-9]*$")) {
//								if (month.matches("^[0-9]*$") || (month.startsWith("W") && month.substring(1).matches("^[0-9]*$")))
//									flagTransformable = true;
//								else if (month.equals("SP")) {
//									dateOfTE = year + "-03-20";	// CHUN FEN
//									flagTransformable = true;
//								} else if (month.equals("SU")) {
//									dateOfTE = year + "-06-21"; // XIA ZHI
//									flagTransformable = true;
//								} else if (month.equals("FA")) {
//									dateOfTE = year + "-09-23"; // QIU FEN
//									flagTransformable = true;
//								} else if (month.equals("WI")) {
//									dateOfTE = year + "-12-21"; // DONG ZHI, CHI JIAO ZI ^_^
//									flagTransformable = true;
//								}
//							}
//						} else if (datePart.length >= 3) {
//							String year = datePart[0];
//							String month = datePart[1];
//							String day;
//							if (datePart[2].length() <= 2)
//								day = datePart[2];
//							else
//								day = datePart[2].substring(0, 2);
//							dateOfTE = year + "-" + month + "-" + day;
//							if (year.matches("^[0-9]*$") && month.matches("^[0-9]*$") && day.matches("^[0-9]*$"))
//								flagTransformable = true;
//						}
//						
//						if (flagTransformable) {
//							DateTime timeValue = new DateTime(dateOfTE);
//							//	To make sure the TE is meaningful, I set a meaningful timespan, from 1900-01-01 to 2100-12-31.
//							DateTime timeMinMeaningful = new DateTime("1900-01-01");
//							DateTime timeMaxMeaningful = new DateTime("2100-12-31");
//							if (timeValue.isAfter(timeMinMeaningful) && timeValue.isBefore(timeMaxMeaningful)) {
//								//	public int numYearsExpTE[] = new int[17];	//	The number of explicit temporal expressions whose years are in 1996-2012
//								int year = timeValue.getYear();
//								if (year >= 1996 && year <= 2012)
//									paragraphFeature.numYearsExpTE[(year-1996)]++;
//								
//								//	Explicit temporal expressions
//								if (valEarliestExpTE == null || timeValue.isBefore(valEarliestExpTE))
//									valEarliestExpTE = timeValue;
//						
//								if (valLatestExpTE == null || timeValue.isAfter(valLatestExpTE))
//									valLatestExpTE = timeValue;
//							
//								if (valClosestExpTE == null)
//									valClosestExpTE = timeValue;
//								else {
//									int daysGap1 = Days.daysBetween(timeValue, pageTime).getDays();
//									int daysGap2 = Days.daysBetween(valClosestExpTE, pageTime).getDays();
//									if (Math.abs(daysGap1) < Math.abs(daysGap2))
//										valClosestExpTE = timeValue;	
//								}
//							}
//						}
//					}					
//				}
//				if (valEarliestExpTE != null)
//					paragraphFeature.valEarliestExpTE = Days.daysBetween(baseTime, valEarliestExpTE).getDays();
//				if (valLatestExpTE != null)
//					paragraphFeature.valLatestExpTE = Days.daysBetween(baseTime, valLatestExpTE).getDays();
//				if (valLatestExpTE != null)
//					paragraphFeature.valClosestExpTE = Days.daysBetween(baseTime, valClosestExpTE).getDays();
//				if (valEarliestExpTE != null && valLatestExpTE != null)
//					paragraphFeature.valSpanExpTE = Days.daysBetween(valEarliestExpTE, valLatestExpTE).getDays();				
//			}
			
			ParagraphWithFeatures paragraphWithFeatures = new ParagraphWithFeatures();
			paragraphWithFeatures.paragraph = paragraph;
			paragraphWithFeatures.features = paragraphFeature;
			paragraphWithFeaturesList.add(paragraphWithFeatures);
		}
		//	--------- End the Procedure for each paragraph ---------		
		return paragraphWithFeaturesList;
	}

}
