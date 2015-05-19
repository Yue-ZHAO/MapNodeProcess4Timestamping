package yue.temporal.MapProcess;

public class ParagraphPrediction {

	public String docID;
	public String content;
	public String timestamps;
	public double confidenceValue;
	
	public ParagraphPrediction() {
		docID = "";
		content = "";
		timestamps = "";
		confidenceValue = 0;
	}
	
	public String toString() {
		String string = docID + "\t" + timestamps + "\t" + confidenceValue + "\n" + content;
		return string;
	}

}
