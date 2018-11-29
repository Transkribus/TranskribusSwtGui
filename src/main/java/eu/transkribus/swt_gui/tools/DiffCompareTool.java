package eu.transkribus.swt_gui.tools;

import java.util.ArrayList;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DiffCompareTool {
	
		public String result = "";
	        public String getResult() {
			return result;
		}
	        
        private final static Logger logger = LoggerFactory.getLogger(DiffCompareTool.class);
        
		private ArrayList<String> similarWordList;
        
        private static final String INSERT_COLOR_green = "#8bf2a6";
        private static final String DELETE_COLOR_orange = "#f2a68b";
        
        public DiffCompareTool(Display display, ArrayList<String> htrTextArray, ArrayList<String> correctTextArray) {
        	
        	
        	//fill-up array to have similar size
        	if (htrTextArray.size() > correctTextArray.size()){
        		int i = (correctTextArray.size() == 0 ? 0 : correctTextArray.size()-1);
        		for (; i<(htrTextArray.size()-1); i++){
        			correctTextArray.add("");
        		}
        	}
        	else if (correctTextArray.size() > htrTextArray.size()){
        		int i = (htrTextArray.size() == 0 ? 0 : htrTextArray.size()-1);
        		for (; i<(correctTextArray.size()-1); i++){;
        			htrTextArray.add("");
        		}
        	}
        	        	
        	int i = 0;
        	for (String htrText : htrTextArray){
        		String correctText = correctTextArray.get(i);
        		i++;
        		
                htrText = normalizeText(htrText);
                correctText = normalizeText(correctText);
                this.similarWordList = getSimilarWordList(htrText, correctText);
                result += markTextDifferences(htrText, correctText,
                        similarWordList, INSERT_COLOR_green, DELETE_COLOR_orange);
                result += "<br>";
        	}
        }

        private ArrayList<String> getSimilarWordList(String htrText, String correctText) {
            String[] htrWords = htrText.split(" ");
            String[] correctWords = correctText.split(" ");
            int htrWordCount = htrWords.length;
            int correctWordCount = correctWords.length;
           
            int[][] helperMatrix = new int[htrWordCount + 1][correctWordCount + 1];
           
            for (int i = htrWordCount - 1; i >= 0; i--) {
                for (int j = correctWordCount - 1; j >= 0; j--) {
                    if (htrWords[i].equals(correctWords[j])) {
                        helperMatrix[i][j] = helperMatrix[i + 1][j + 1] + 1;
                    }
                    else {
                        helperMatrix[i][j] = Math.max(helperMatrix[i + 1][j],
                            helperMatrix[i][j + 1]);
                    }
                }
            }
           
            int i = 0, j = 0;
            ArrayList<String> resultList = new ArrayList<String>();
            while (i < htrWordCount && j < correctWordCount) {
                if (htrWords[i].equals(correctWords[j])) {
                    resultList.add(correctWords[j]);
                    i++;
                    j++;
                }
                else if (helperMatrix[i + 1][j] >= helperMatrix[i][j + 1]) {
                    i++;
                }
                else {
                    j++;
                }
            }
            return resultList;
        }
       
        private String normalizeText(String text) {
           
            text = text.trim();
            text = text.replace("\n", " ");
            text = text.replace("\t", " ");        
//            if(text.contains("\u202F") || text.contains("\u00A0")){
//            	logger.debug("text contains non-break space");
//            }         
            text = text.replace("\u00A0", " ");
            text = text.replace("\u202F", " ");
           
            while (text.contains("  ")) {
                text = text.replace("  ", " ");
            }
            return text;
        }

        private String markTextDifferences(String htrText, String correctText,
            ArrayList<String> similarWords, String insertColor, String deleteColor) {
            StringBuffer stringBuffer = new StringBuffer();
            if (htrText != null && similarWords != null) {
                String[] htrWords = htrText.split(" ");
                String[] correctWords = correctText.split(" ");
                int i = 0, j = 0, htrWordLastIndex = 0, correctWordLastIndex = 0;
                for (int k = 0; k < similarWords.size(); k++) {
                    for (i = htrWordLastIndex, j = correctWordLastIndex;
                        i < htrWords.length && j < correctWords.length;) {
                        if (htrWords[i].equals(similarWords.get(k)) &&
                            correctWords[j].equals(similarWords.get(k))) {
                            stringBuffer.append("<SPAN>" + similarWords.get(k) + " </SPAN>");
                            htrWordLastIndex = i + 1;
                            correctWordLastIndex = j + 1;
                            i = htrWords.length;
                            j = correctWords.length;
                        }
                        else if (!htrWords[i].equals(similarWords.get(k))) {
                            for (; i < htrWords.length &&
                                !htrWords[i].equals(similarWords.get(k)); i++) {
                                stringBuffer.append("<SPAN style='BACKGROUND-COLOR:" +
                                    deleteColor + "'><DEL>" + htrWords[i] + " </DEL></SPAN>");
                            }
                        } else if (!correctWords[j].equals(similarWords.get(k))) {
                            for (; j < correctWords.length &&
                                !correctWords[j].equals(similarWords.get(k)); j++) {
                                stringBuffer.append("<SPAN style='BACKGROUND-COLOR:" +
                                    insertColor + "'>" + correctWords[j] + " </SPAN>");
                            }
                        }
                    }
                }
                for (; htrWordLastIndex < htrWords.length; htrWordLastIndex++) {
                    stringBuffer.append("<SPAN style='BACKGROUND-COLOR:" +
                        deleteColor + "'><DEL>" + htrWords[htrWordLastIndex] + " </DEL></SPAN>");
                }
                for (; correctWordLastIndex < correctWords.length; correctWordLastIndex++) {
                    stringBuffer.append("<SPAN style='BACKGROUND-COLOR:" +
                        insertColor + "'>" + correctWords[correctWordLastIndex] + " </SPAN>");
                }
            }
            return stringBuffer.toString();
        }
        
}


