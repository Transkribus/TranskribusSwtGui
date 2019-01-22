package eu.transkribus.util;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SebisClassloader extends URLClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(SebisClassloader.class);
	
    public SebisClassloader(ClassLoader parent) {
        super(new URL[0], parent);
        logger.info("parent classloader: "+parent);
    }
	
    public SebisClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addURL(URL url) {
        super.addURL(url);
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    	System.out.println("LEGIT - "+name+" resolve = "+resolve+" whoami = "+this);
    	return super.loadClass(name, true);
    }
    
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
    	System.out.println("finding class: "+name);
    	
    	if (name.startsWith("org.eclipse.swt")) {
    		System.out.println("this is an swt class: "+name);
    		URLClassLoader urlClassloader = new URLClassLoader(getURLs(), null);
    		return Class.forName(name, true, urlClassloader);
//    		return urlClassloader.findClass(name);
    	}
    	
    	return super.findClass(name);
    }
}