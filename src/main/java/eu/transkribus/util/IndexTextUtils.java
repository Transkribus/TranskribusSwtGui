package eu.transkribus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.pagecontent.BaselineType;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.TextRegionType;
import eu.transkribus.core.model.beans.pagecontent.WordType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.util.PageXmlUtils;
/**
 * Class containing various tools used when indexing transcript documents
 */
public class IndexTextUtils {	
	private static final Logger logger = LoggerFactory.getLogger(IndexTextUtils.class);
	
	/**
	 * Generates Trp Words with positions from a Trp Line. 
	 * Coordinates are estimated from baseline coordinates and 
	 * the relative word positions inside the line text string
	 * @param	line	Trp text line
	 * @return	List of TrpWordType in text line
	 */
	public static ArrayList<TrpWordType> getWordsFromLine(TrpTextLineType line){
		
		ArrayList<TrpWordType> trpWords = new ArrayList<TrpWordType>();
		if (line == null){
			return trpWords;
		}
		
		if(line.getBaseline() == null){
			line.setBaseline(generateBaseline(line));
		}
				
		
		String baseLine = line.getBaseline().getPoints();
		String string = line.getUnicodeText();	
		string = string.replaceAll("-"," ").replaceAll("\\p{Punct}", ".").replaceAll("¬", ".");
		
		String[] basePts = baseLine.trim().split(" ");

		
		ArrayList<Integer> xPts = new ArrayList<Integer>();
		ArrayList<Integer> yPts = new ArrayList<Integer>();		
		
		for(String s: basePts){
			String[] coords = s.split(",");
			try {
				xPts.add(Integer.parseInt(coords[0]));					//Baseline X coords
				yPts.add(Integer.parseInt(coords[1]));					//Baseline Y coords
			} catch(Exception e ){
				logger.error("Could not parse String: " + s, e);
				logger.error("Baseline = " + baseLine);
				throw e;
			}
		}
		int baseLen = Math.abs(xPts.get(xPts.size()-1)-xPts.get(0)); //Length of  baseline in px
		int baseStartX = xPts.get(0);
		
//		int charLen = Math.round(((float)baseLen)/((float)string.length()));		
//		int baseAvgY = getAverage(yPts);	
		
		int wordCounter = 0;
		for(String s : string.split(" ")){
			wordCounter++;
			int subIndex = string.indexOf(s);										//Position of word in string
			float subStart = (float) subIndex / (float)string.length();				//Relative start position 
			int subStartPx = (int) (subStart*(float)baseLen);						//Relative start position in px
			float subLength = (float) s.length() / (float) string.length();			//Length of word
			int subLengthPx = (int) (subLength*(float)baseLen);						//Length of word in px
			int subHeightPx = (int)((float) baseLen / (float)string.length() * 2.0);//Height of word in px (est. 3 characters)
			int nearestIndex = getNearestIndex(xPts, subStartPx+xPts.get(0));		//Find index of nearest x in basepts	
			int baseY = yPts.get(nearestIndex);										//Get y position of nearest x coord
			int wordCoordY1 = baseY + (int)((float) subHeightPx / 4.0);				//Y coordinates of baseline
			int wordCoordY2 = baseY - subHeightPx;									//Y coordinates of word ceiling				
			int wordCoordX1 = (baseStartX + subStartPx);							//X ccordinates of word start
			int wordCoordX2 =  baseStartX + subStartPx + subLengthPx;				//X coordinates of word end		
			
			String outputCoords =  wordCoordX1 + "," + wordCoordY1 
								+ " " + wordCoordX2 + "," + wordCoordY1
								+ " " + wordCoordX2 + "," + wordCoordY2
								+ " " + wordCoordX1 + "," + wordCoordY2;								
								
			String outputWord = s.replaceAll("\\p{Punct}", "").trim(); 				//Remove punctuation / set lowercase / trim
			
			TrpWordType trpWord = new TrpWordType();
			if(trpWord != null && outputWord != ""){			
				trpWord.setParent(line);
				trpWord.setId(line.getId() + "_" + wordCounter);
				trpWord.setUnicodeText(outputWord, trpWord);
				trpWord.setCoordinates(outputCoords, trpWord);
				trpWord.setLine(line);
				trpWords.add(trpWord);
			}

			String replacement = "";			
			for(int i = 0; i< s.length(); i++){
				replacement +=" " ;
			}		
			string = string.replaceFirst(s, replacement); //Replace word chars with empty spaces	
		}
		return trpWords;
	}
	
	/**
	 * Find the list index of point in xPts list that is closest to posX
	 * @param xPts List of coordinates
	 * @param posX Point
	 * @return Index position of nearest point
	 */
	private static int getNearestIndex(ArrayList<Integer> xPts, int posX) {
		int indexPos = 0;
		int minDist = Math.abs(xPts.get(0)-posX);
		for(int i=1; i < xPts.size(); i++){
			if(Math.abs(xPts.get(i)-posX) < minDist){
				indexPos = i;
			}
		}		
		return indexPos;
	}
	
	/*
	 * Generate average value of points and round to nearest integer
	 */
	public static int getAverage(ArrayList<Integer> points){
		float output = 0;
		for(Integer i : points){
			output += i;
		}
		output /= (float)points.size();
		
		return Math.round(output);
	}
	
	/*
	 * Generate a BaselineType from a Trp TextLine.
	 * Estimates baseline coordinates from line coordinates
	 */
	private static BaselineType generateBaseline(TrpTextLineType line) {
		
		BaselineType baseLine = new BaselineType();
		ArrayList<Integer> xPts = new ArrayList<Integer>();
		ArrayList<Integer> yPts = new ArrayList<Integer>();		
		String[] singleCoords = line.getCoordinates().split(" ");
		
		for(String s: singleCoords){
			String[] coords = s.split(",");
			xPts.add(Integer.parseInt(coords[0]));					//All X coords
			yPts.add(Integer.parseInt(coords[1]));					//All Y coords
		}
		int CoordX1 = xPts.get(0);
		int CoordX2 = xPts.get(0);
		int CoordY1 = yPts.get(0);
		
		//find outlining points
		for(int x : xPts){
			if(CoordX1>x){
				CoordX1 = x;
			}
			if(CoordX2<x){
				CoordX2 = x;
			}
		}
		CoordY1=getAverage(yPts);
		
		baseLine.setPoints(CoordX1+","+CoordY1+" "+CoordX2+","+CoordY1);
		return baseLine;
	}

	@Deprecated
	public static String getFullText(PcGtsType pc){
		String fullText = "";
		for(TextRegionType tr : PageXmlUtils.getTextRegions(pc)){
				fullText += tr.getUnicodeText();	
		}		
		return fullText;
	}
	
	
	/**
	 * Takes arbitrary coordinate string array and 
	 * returns 4 coordinate points on outline edges
	 * @param singleCoords String array of coordinates, separated with a comma
	 * @return Complete string of reduced x and y coordinates
	 */
	public static String reduceCoordinates(String[] singleCoords){
			ArrayList<Integer> xPts = new ArrayList<Integer>();
			ArrayList<Integer> yPts = new ArrayList<Integer>();		

			for(String s: singleCoords){
				String[] coords = s.split(",");
				xPts.add(Integer.parseInt(coords[0]));					//All X coords
				yPts.add(Integer.parseInt(coords[1]));					//All Y coords
			}
			int wordCoordX1 = xPts.get(0);
			int wordCoordX2 = xPts.get(0);
			int wordCoordY1 = yPts.get(0);
			int wordCoordY2 = yPts.get(0);
			
			
			for(int x : xPts){
				if(wordCoordX1>x){
					wordCoordX1 = x;
				}
				if(wordCoordX2<x){
					wordCoordX2 = x;
				}
			}
			for(int y : yPts){
				if(wordCoordY1<y){
					wordCoordY1 = y;
				}
				if(wordCoordY2>y){
					wordCoordY2 = y;
				}
			}
			
			String outputCoords =  wordCoordX1 + "," + wordCoordY1 
					+ " " + wordCoordX2 + "," + wordCoordY1
					+ " " + wordCoordX2 + "," + wordCoordY2
					+ " " + wordCoordX1 + "," + wordCoordY2;
			
			
			return outputCoords;
		}
	
	
	/**
	 * Get coordinates of a word by opening transcript of TrpPage p 
	 * and comparing String word to all words on page.
	 */
	@Deprecated
	public static Map<String, String> getPixelCoordinates(String word, TrpPage p){
		
		Map<String,String> coords = new HashMap<String,String>();
		PcGtsType pc = new PcGtsType();
		try {
			pc = PageXmlUtils.unmarshal(p.getCurrentTranscript().getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		for(TextRegionType tr: PageXmlUtils.getTextRegions(pc)){
			for(TextLineType tl : tr.getTextLine()){
				TrpTextLineType ttl = (TrpTextLineType) tl;
				if(ttl.getWordCount() > 0){
					for(WordType tw : ttl.getWord()){
						TrpWordType ttw = (TrpWordType) tw;
						if(ttw.getUnicodeText().trim().replaceAll("\\p{Punct}", "").equals(word)){
							coords.put(tr.getId()+":"+tl.getId()+":"+ttw.getId(), ttw.getCoordinates());
						}
					}
				}else{
					List<TrpWordType> lineWords = IndexTextUtils.getWordsFromLine(ttl);
					for(TrpWordType tw : lineWords){
						if(tw.getUnicodeText().trim().replaceAll("\\p{Punct}", "").equals(word)){
							coords.put(tr.getId()+":"+tl.getId(), tw.getCoordinates());
						}
					}
					
				}
			}
		}		
		
		return coords;
	}	

}

