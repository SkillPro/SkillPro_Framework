package com.eztech.util;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * Utility to walk the Java classpath, and to find all classes which are assignable (i.e. inherit) 
 * a specified class. If no matching class is specified, will return all classes in the classpath
 * @author Sam
 *
 */
public class JavaClassFinder  {
	public static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";
	public static final String CUSTOM_CLASS_PATH_PROPERTY = "custom.class.path";

	//	private static Logger LOG = Logger.getLogger(JavaClassFinder.class);

	private ArrayList<Class<?>> foundClasses;
	private Class<?> toFind;
	private JavaClassFileWalker fileWalker;
	private ClassLoadingFileHandler fileHandler;
	private ClassLoader loader;

	/**
	 * Finds all classes which are Assignable from the specified class
	 * @param toFind only classes which are subtypes or implementers of the this class are found
	 * @return List of class objects
	 */
	@SuppressWarnings("unchecked")
	public <T> List<Class<? extends T>> findAllMatchingTypes(Class<T> toFind, ClassLoader loader) {
		this.loader = loader;
//		System.out.println("loader:  "+loader);
		foundClasses = new ArrayList<Class<?>>();
		List<Class<? extends T>> returnedClasses = new ArrayList<Class<? extends T>>();
		this.toFind = toFind;
		walkClassPath();
		for (Class<?> clazz : foundClasses) {
			returnedClasses.add((Class<? extends T>) clazz);
		}
		return returnedClasses;
	}

	private void walkClassPath() {
		fileHandler = new ClassLoadingFileHandler();
		fileWalker = new JavaClassFileWalker(fileHandler);

		String[] classPathRoots = getClassPathRoots();
		for (int i=0; i< classPathRoots.length; i++) {
			String path = classPathRoots[i];
			if (path.endsWith(".jar") || path.endsWith(".jar!/")) {
//				System.out.println("walkClassPath(): reading from jar not yet implemented, jar file=" + path);
//				System.out.println("the new base dir: " + path);
				//				LOG.warn("walkClassPath(): reading from jar not yet implemented, jar file=" + path);
//				continue;
				doJar(path);
			} else {
				fileHandler.updateClassPathBase(path, loader);
				fileWalker.setBaseDir(path);
				fileWalker.walk();
			}
			//			LOG.debug("walkClassPath(): checking classpath root: " + path);
			// have to reset class path base so it can instance classes properly
			
		}			
	}
	
	private void doJar(String path) {
		try {
			JarInputStream jar = new JarInputStream(new FileInputStream(path.replace("!/", "").replace("jar:", "")));
			JarEntry jarEntry = null;
			//saves all the contents of the jar entry with the help of the helper class JarResources
			JarResources res = null;
			try {
				res = new JarResources(path.replace("!/", "").replace("jar:", ""));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//the loop will stop for certain
			while (true) {
				jarEntry = jar.getNextJarEntry();
				if (jarEntry == null) {
					break;
				}
				
				if (jarEntry.getName().endsWith(".class")) {
					String realPath = jarEntry.getName().replaceAll("/", "\\.");
					//creates a temp file just to extract the temp directory
					File tempOfTheTemp = File.createTempFile("temp", ".class");
					String parentDir = tempOfTheTemp.getParent();
					if (!parentDir.endsWith("/")) {
						parentDir = parentDir + "/";
					}
					//real path looks like: eu.skillpro.amls.pscm.transformables.model.ITransformable.class
					File tempFile = new File(parentDir + realPath);
					//delete the temp of the temp file
					tempOfTheTemp.delete();
					FileOutputStream os = new FileOutputStream(tempFile);
					//get the content of the jar entry and write it into the tempFile.
					os.write(res.getResource(realPath));
					os.close();
//					System.out.println("JAR ENTRY: " + realPath);
					fileHandler.updateClassPathBase(parentDir.replace("\\", "/"), loader);
					fileHandler.handleFile(tempFile);
					//delete the temp file after we've found the classes we need
					tempFile.delete();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] getClassPathRoots() {
		String classPath;
		if (System.getProperties().containsKey(CUSTOM_CLASS_PATH_PROPERTY)) {
			//			LOG.debug("getClassPathRoots(): using custom classpath property to search for classes");
			classPath = System.getProperty(CUSTOM_CLASS_PATH_PROPERTY);
//			System.out.println("IS THIS THE CLASSPATH?? " + classPath);
		} else {
			classPath = System.getProperty(JAVA_CLASS_PATH_PROPERTY);
		}
		String[] pathElements = classPath.split(File.pathSeparator);
		//		LOG.debug("getClassPathRoots(): classPath roots=" + StringUtil.dumpArray(pathElements));
		return pathElements;
	}

	private void handleClass(Class<?> clazz) {
		boolean isMatch = false;
//		System.out.println("WHAT IS THIS CLASS: " + clazz.getName());
		isMatch = toFind == null || toFind.isAssignableFrom(clazz);
		if (isMatch) {
			foundClasses.add(clazz);
		}
	}


	/**
	 * FileFindHandler plugin for the JavaClassFileWalker object to 
	 * create a class object for matched class files
	 * @author Sam
	 *
	 */
	class ClassLoadingFileHandler extends FileFindHandlerAdapter {
		private FileToClassConverter converter;
		private ClassLoader loader;
		
		public void updateClassPathBase(String classPathRoot, ClassLoader loader) {
			if (converter == null) {
				converter = new FileToClassConverter(classPathRoot);
			}
			this.loader = loader;
			converter.setClassPathRoot(classPathRoot);
		}
		@Override
		public void handleFile(File file) {
			// if we get a Java class file, try to convert it to a class
//			System.out.println("RUEAK: " + file.getAbsolutePath());
			Class<?> clazz = converter.convertToClass(file, loader);
//			System.out.println("AFTER CONvERTING: " + clazz);
			if (clazz == null) {
				return;
			}	
			handleClass(clazz);
		}
	}


	/**
	 * Finds all classes in the classpath
	 * @return
	 */
	public List<Class<?>> findAllMatchingTypes() {
		return findAllMatchingTypes(null, null);
	}

	public int getScannedClassesCount() {
		if (fileWalker == null) {
			return 0;
		}
		return fileWalker.getAllFilesCount();
	}
	
	//helper class to extract the resources
	public final class JarResources {
		private Hashtable<String, Integer> htSizes = new Hashtable<String, Integer>();
		private Hashtable<String, byte[]> htJarContents = new Hashtable<String, byte[]>();
		private String jarFileName;

		public JarResources(String jarFileName) throws Exception {
			this.jarFileName = jarFileName;
			ZipFile zf = new ZipFile(jarFileName);
			Enumeration<?> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();

				htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
			}
			zf.close();

			// extract resources and put them into the hashtable.
			FileInputStream fis = new FileInputStream(jarFileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ZipInputStream zis = new ZipInputStream(bis);
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				if (ze.isDirectory()) {
					continue;
				}

				int size = (int) ze.getSize();
				// -1 means unknown size.
				if (size == -1) {
					size = ((Integer) htSizes.get(ze.getName())).intValue();
				}

				byte[] b = new byte[(int) size];
				int rb = 0;
				int chunk = 0;
				while (((int) size - rb) > 0) {
					chunk = zis.read(b, rb, (int) size - rb);
					if (chunk == -1) {
						break;
					}
					rb += chunk;
				}
				String realPath = ze.getName().replaceAll("/", "\\.");
//				System.out.println("NAMEEEEEEEE: " + realPath);
				htJarContents.put(realPath, b);
			}
		}

		public byte[] getResource(String name) {
			return htJarContents.get(name);
		}
		
		public String getJarFileName() {
			return jarFileName;
		}
	}

}

