package com.eztech.util;

import java.io.File;

/**
 * Convert a File object to a Class
 * 
 * @author Sam
 * 
 */
public class FileToClassConverter {

	private String classPathRoot;

	public FileToClassConverter(String classPathRoot) {
		setClassPathRoot(classPathRoot);
	}

	/**
	 * @param classPathRoot
	 */
	public void setClassPathRoot(String classPathRoot) {
		if (classPathRoot == null) {
			throw new RuntimeException("Class path root must not be null");
		}
		this.classPathRoot = classPathRoot;
	}

	public Class<?> convertToClass(File classFile, ClassLoader loader) {
		Class<?> classInstance = null;
//		System.out.println("Convert!!!");
//		System.out.println("Class path root: " + classPathRoot);
//		System.out.println("class file: " + classFile.getAbsolutePath());
		String path = classFile.getAbsolutePath().replace("\\", "/");
		if (path.startsWith(classPathRoot) && classFile.getAbsolutePath().endsWith(".class")) {
//			System.out.println("Hello???");
			classInstance = getClassFromName(classFile.getAbsolutePath(), loader);
//			System.out.println("class instance=== " + classInstance.getName());
		}
		return classInstance;
	}

	private Class<?> getClassFromName(String fileName, ClassLoader loader) {
		try {
//			System.out.println("Yes???");
			String className = removeClassPathBase(fileName);
			className = FileUtils.removeExtension(className);
//			Class clazz = Class.forName("com.eztech.util.FileUtils");
////			System.out.println("CLAZZ: " + clazz.getName());
//			clazz = Class.forName("aml.domain.Domain");
//			System.out.println("CLAZZ: " + clazz.getName());
//			clazz = Class.forName("test.classes.Class1");
//			System.out.println("CLAZZ: " + clazz.getName());
//			clazz = Class.forName("java.util.List");
//			System.out.println("CLAZZ: " + clazz.getName());
//			System.out.println(className);
//			return Class.forName(className);
//			File root = new File(".");
//			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			return Class.forName(className, true, loader);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param fileName
	 * @return
	 */
	private String removeClassPathBase(String fileName) {
		String classPart = fileName.substring(classPathRoot.length());
		String className = classPart.replace(File.separatorChar, '.');
		return className;
	}



}
