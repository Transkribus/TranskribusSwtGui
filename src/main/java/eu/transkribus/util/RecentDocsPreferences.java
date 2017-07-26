package eu.transkribus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class RecentDocsPreferences {
	    
	    public final static String RECENT_DOC_STRING = "LAST_DOC_";
	    
	    static Preferences prefNode = Preferences.userRoot().node( "/trp/recent_docs" );
	    private static int	nrDocs = 10;
	    
	    public static boolean showOnStartup = false;

	    public static boolean isShowOnStartup() {
			return showOnStartup;
		}

		public static void setShowOnStartup(boolean showOnStartup) {
			RecentDocsPreferences.showOnStartup = showOnStartup;
		}

		private static List<String> values = new ArrayList<String>();
	    
//	    public RecentDocsPreferences(int maxDocs, Preferences pref)
//	    {
//	        nrDocs = maxDocs;
//	        prefNode = pref;
//	        
//	        loadFromPreferences();
//	    }
	    
	    public static void init(){
	    	loadFromPreferences();
	    }
	    
	    public static void push(String item)
	    {
	        values.remove(item);
	        values.add(0, item);
	        
	        if (values.size() > nrDocs)
	        {
	            values.remove(values.size() - 1);
	        }
	        
	        update();
	    }
	    
	    public void remove(Object item)
	    {
	        values.remove(item);
	        update();
	    }
	    
	    public String get(int index)
	    {
	        return (String)values.get(index);
	    }
	    
	    public static List<String> getItems()
	    {
	        return values;
	    }
	    
	    public int size()
	    {
	        return values.size();
	    }
	    	    
	    private static void update()
	    {	        
	        storeToPreferences();
	    }
	    
	    private static void loadFromPreferences()
	    {
	        for (int i = 0; i < nrDocs; i++)
	        {
	        	
	            String val = prefNode.get(RECENT_DOC_STRING+i, "");

	            if (!val.equals(""))
	            {
	                values.add(val);
	            }
	            else
	            {
	                break;
	            }
	        }
	    }
	    
	    private static void storeToPreferences()
	    {
	        for (int i = 0; i < nrDocs; i++)
	        {
	            if (i < values.size())
	            {
	            	prefNode.put(RECENT_DOC_STRING+i, (String)values.get(i));
	            }
	            else
	            {
	            	prefNode.remove(RECENT_DOC_STRING+i);
	            }
	        }
	    }
	}
