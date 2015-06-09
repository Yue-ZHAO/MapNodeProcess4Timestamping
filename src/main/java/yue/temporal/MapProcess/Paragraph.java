package yue.temporal.MapProcess;

public class Paragraph {

	//	the start position of the paragraph
	private int startPoint = -1;
	
	//	the end position of the paragraph
	private int endPoint = -1;
	
	//	the content
	private String content = "";
	
	//	the time stamp
	private String timestamp = "";
	
	public String featureToString() {
		String feature = getStartPoint() + " " + getEndPoint() + " " + getTimestamp();
		return feature;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(int startPoint) {
		this.startPoint = startPoint;
	}

	public int getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(int endPoint) {
		this.endPoint = endPoint;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
