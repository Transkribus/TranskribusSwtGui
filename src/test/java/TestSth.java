import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mkobos.pca_transform.PCA;
import com.mkobos.pca_transform.PCA.TransformationType;

import Jama.Matrix;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.LocalDocWriter;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.mets.Mets;
import eu.transkribus.core.model.beans.pagecontent.OrderedGroupIndexedType;
import eu.transkribus.core.model.beans.pagecontent.OrderedGroupType;
import eu.transkribus.core.model.beans.pagecontent.ReadingOrderType;
import eu.transkribus.core.model.beans.pagecontent.RegionRefIndexedType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.builder.mets.TrpMetsBuilder;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.util.DesktopApi;


public class TestSth {
	private final static Logger logger = LoggerFactory.getLogger(TestSth.class);
	
	public static class A {
		String s = "hello";
		
		public A() {
			
		}
		
		public A(A other) {
			this.s = other.s;
		}
	}
		
	public static void createTestDoc() throws IOException {
//		String imgLoc = "/Users/hansm/Documents/testDocs/Bentham_box_002/002_080_001.jpg";
		String imgLoc = "/home/sebastian/Transkribus_TestDoc/035_320_001.jpg";
		
		String newDocLoc = "/home/sebastian/Documents/transkribus_testdocs/many_pages/";
		
		File f = new File(imgLoc);
		for (int i=0; i<2000; ++i) {
			File nf = new File(newDocLoc+"/"+(i+1)+".jpg");
			System.out.println(nf.getAbsolutePath());
			
			FileUtils.copyFile(f, nf);
			
			
		}
		
		
	}
	
	public static void testPCABaselineCorrection() {
		Matrix trainingData = new Matrix(new double[][] {
//            {1, 2, 3, 4, 5, 6},
//            {6, 5, 4, 3, 2, 1},
//            {2, 2, 2, 2, 2, 2}}
		
        {4, 2},
        {2, 4}
//        {2, 2, 2, 2, 2, 2}		
		});
		
		PCA pca = new PCA(trainingData);
		
		Matrix tm = pca.transform(trainingData, TransformationType.WHITENING);
		
		pca.getEigenvectorsMatrix().print(2, 2);
		tm.print(2, 2);
		
		
//		pca.transform(data, TransformationType.WHITENING);
		
		
	}
	
	public static void testDownloadClientFileNew(String un, String pw) throws Exception {
		TrpServerConn conn = new TrpServerConn(TrpServerConn.TEST_SERVER_URI);
		conn.login(un, pw);

		File f = new File("testDownload.zip");
//		Map<String, String> libs = ProgramUpdater.getLibs(true);
		Map<String, String> libs = new HashMap<>();
		libs.put("whatever", "yeah");
		libs.put("whatever1", "yeah2");
		
		logger.info("before download");
		conn.downloadClientFileNew(false, "Transkribus-0.6.6.5-SNAPSHOT-package.zip", f, libs, null);
		logger.info("after download");
		
		conn.close();
	}	
	
	public static void testDownloadClientFile(String un, String pw) throws Exception {
		TrpServerConn conn = new TrpServerConn(TrpServerConn.OLD_TEST_SERVER_URI);
		conn.login(un, pw);

		File f = new File("testDownload.zip");
//		Map<String, String> libs = ProgramUpdater.getLibs(true);
		Map<String, String> libs = new HashMap<>();
		libs.put("whatever", "yeah");
		libs.put("whatever1", "yeah2");
		
		logger.info("before download");
		conn.downloadClientFile(false, "Transkribus-0.6.6.3-SNAPSHOT-package.zip", f, libs, null);
		logger.info("after download");
		
		conn.close();
	}
	
	public static void testEditFile() {
		File file = new File("/media/dea_scratch/TRP/TrpTestDoc_20131209/page/StAZ-Sign.2-1_001.xml");
		try {
			if (!DesktopApi.edit(file))
				throw new Exception("Cannot open file in editor: "+file.getAbsolutePath());
//			java.awt.Desktop.getDesktop().edit(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int[] extractVersion(String filename) {
//		filename.split(regex)
		// TODO
		
		return null;
		
	}
	
	public static String parseVersion(String str) {
		String regex = ".*-(\\d+(\\.\\d+)*(\\.\\d+[a-zA-Z-_]*)?)-.*";
		
		Pattern pattern = Pattern.compile(regex);
		
		Matcher matcher = pattern.matcher(str);
		
		System.out.println("matches: "+matcher.matches());
		if (matcher.matches())
		for (int i=0; i<matcher.groupCount(); ++i) {
			System.out.println("group "+i+": "+matcher.group(i));
			
		}
		
//		while (matcher.find()) {
//		      System.out.print("Start index: " + matcher.start());
//		      System.out.print(" End index: " + matcher.end() + " ");
//		      System.out.println(matcher.group());
//		    }
		
		return "";
	}

	public static void testReadingOrder() throws Exception {
		TrpDoc doc = LocalDocReader.load("/media/dea_scratch/TRP/Schauplatz_Small2/", true, true, true, true, false);
		
		TrpPage p = doc.getPages().get(0);
		JAXBPageTranscript tr = new JAXBPageTranscript(p.getCurrentTranscript());
		tr.build();
		
		TrpPageType page = (TrpPageType) tr.getPageData().getPage();
		
		ReadingOrderType ro = new ReadingOrderType();
		page.setReadingOrder(ro);
		OrderedGroupType og = new OrderedGroupType();
		ro.setOrderedGroup(og);
		
		RegionRefIndexedType rri = new RegionRefIndexedType();
		rri.setIndex(0);
		rri.setRegionRef(page.getTextRegions(false).get(0));
		
		og.getRegionRefIndexedOrOrderedGroupIndexedOrUnorderedGroupIndexed().add(rri);
		
		rri = new RegionRefIndexedType();
		rri.setIndex(5);
		rri.setRegionRef(page.getTextRegions(false).get(0).getTextLine().get(0));
		
		og.getRegionRefIndexedOrOrderedGroupIndexedOrUnorderedGroupIndexed().add(rri);
		
		rri = new RegionRefIndexedType();
		rri.setIndex(-1);
//		rri.setRegionRef(page.getRegions().get(0).getTextLine().get(0).getWord().get(0));
		rri.setRegionRef(page.getTextRegions(false).get(0).getTextLine().get(0));
		
		og.getRegionRefIndexedOrOrderedGroupIndexedOrUnorderedGroupIndexed().add(rri);
		
		og.getRegionRefIndexedOrOrderedGroupIndexedOrUnorderedGroupIndexed().add(og);
		
		OrderedGroupIndexedType og1 = new OrderedGroupIndexedType();
		
		// TODO: save page:
		LocalDocWriter.updateTrpPageXml(tr);		
	}
	
	private static void copyAVBefore1926Data() {
		String ip = "/media/iza_retro/DIG_auftraege_archiv/DIG2011/DIG01110011_travel_i/alpenverein_jahrbuecher/alpenverein_jahrbuecher/fertig/";
		String op = "/media/iza_retro/DIG_auftraege_archiv/DIG2011/Travel/4_Alpenverein_Jahrbuecher/Anitqua_vor_1926/";
		
		File ipF = new File(ip);
//		System.out.println(ip);
		
		for (File f : ipF.listFiles()) {
//			System.out.println("f: "+f.getName());
			if (!f.isDirectory())
				continue;
			
			String dn = f.getName();
			
			Pattern p = Pattern.compile("Band_(\\d+)_Vereinsjahr_(\\d+(_\\d+)?)_fertig");
			Matcher m = p.matcher(dn);
//			 System.out.println("matches: "+m.matches()+" dn: "+dn);
			if (m.matches()) {
				String nr = m.group(1);
				String year = m.group(2);
				String newFn = "AV_"+year+"_"+nr;
				System.out.println("newFn = "+newFn);
				
				File of = new File(op+"/"+newFn);
				of.mkdir();
				
				// copy images: 
				File imgDir = new File(f.getAbsolutePath()+"/images/");
				File[] imgs = imgDir.listFiles(new FilenameFilter() {
					
					@Override public boolean accept(File dir, String name) {
						String n = name.toLowerCase();
						return n.endsWith(".tif") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".tiff");
					}
					
					
				});
				int i=1;
				for (File img : imgs) {
					try {
						System.out.println("copying image "+i+" of "+imgs.length+" folder: "+dn);
						FileUtils.copyFile(img, new File(of.getAbsolutePath()+"/"+img.getName()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					++i;
				}
				System.out.println("nr of images: "+imgs.length);
				
//				File newDir = new File(op+"/")
			}
		}
	}
	
	public static void testStrRegexStuff() {
		String r = "asd?";
		r = CoreUtils.createRegexFromSearchString(r, true, true, true);
		System.out.println("r = "+r);
		
		String test = "asdf";
		
//		Pattern p = Pattern.compile(r);
		
		System.out.println(""+test.matches(r));
		
		
	}
	
	public static void testPathNormalization() {
		String path = "C:\\Users\\hansm\\AppData\\Local\\Temp\\/Transkribus/autoSave";
		
		String pathNormalized = FilenameUtils.normalize(path);
		
		System.out.println(path);
		System.out.println(pathNormalized);
		
	}
	
	public static void createMetsForRemoteDoc(String[] args) throws Exception {
		try (TrpServerConn conn = TrpServerConn.connectToProdServer(args[0], args[1])) {
			TrpDoc doc = conn.getTrpDoc(12494, 42174, 1);
			System.out.println("Loaded doc: "+doc.getMd().getTitle()+" nr. of pages: "+doc.getNPages());
			
			Set<Integer> pageIndices = new HashSet<>();
			for (int i=0; i<10; ++i) {
				pageIndices.add(i);	
			}
			
			TrpMetsBuilder metsBuilder = new TrpMetsBuilder();
			Mets mets = metsBuilder.buildMets(doc, true, false, true, pageIndices);
			String outFile = "c:/tmp/mets.xml";
			
			JaxbUtils.marshalToFile(mets, new File(outFile), TrpDocMetadata.class);
//			if (printResultOnSysOut)
			JaxbUtils.marshalToSysOut(mets, TrpDocMetadata.class);
		}
	}
			
	public static void main(String [] args) throws Exception {
		createMetsForRemoteDoc(args);
		
		if (true)
			return;
		
		System.out.println(CitLabSemiSupervisedHtrTrainConfig.isValidTrainingEpochsString(""));
		
		String trainepochs="";
		int[] epochs_inner;
		int epochs = trainepochs.split(";").length;
		
        String[] split = trainepochs.split(";");
        if (split.length == 1) {
            epochs_inner = new int[epochs];
            for (int i = 0; i < epochs_inner.length; i++) {
                epochs_inner[i] = Integer.parseInt(trainepochs);
            }
        } else {
            if (split.length != epochs) {
                throw new RuntimeException("epochs = " + epochs + " but training epochs '" + trainepochs + "' can only be split into " + split.length + " parts.");
            }
            epochs_inner = new int[epochs];
            for (int i = 0; i < split.length; i++) {
                epochs_inner[i] = Integer.parseInt(split[i]);
            }
        }
        
        System.out.println("epochs_inner: ");
        for (int i : epochs_inner) {
        	System.out.println(i);
        }
        
        
        
        if (true)
        	return;
		
		;
		
		for (Path pageXml : CoreUtils.listFilesRecursive("/mnt/dea_scratch/TRP/test/Ms__orient__A_2654/t2iworkdir/trainInput", new String[]{".xml"}, true)) {
			String bn = FilenameUtils.getBaseName(pageXml.getFileName().toString());
			System.out.println(pageXml.getParent().getParent() + File.separator + bn);
		
			
//			laWrapper.process(image, xmlInOut, null, null);
		}
		
//		testPathNormalization();
		if (true)
			return;
		
		try {
			
			A a = new A();
			A b = new A(a);
			
			b.s = "new";
			
//			b.s.s
			
			System.out.println( a.s );
			System.out.println( b.s );
			
			
//			testPCABaselineCorrection();
			
//			createTestDoc();
			
//			System.setProperty("java.library.path", "whatever");
			
//			System.out.println(System.getProperty("java.library.path"));
			
//			testStrRegexStuff();
//			testDownloadClientFile();
//			testDownloadClientFileNew(args[0], args[1]);
//			copyAVBefore1926Data();
			
//			testReadingOrder();
//			System.out.println("Done!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
