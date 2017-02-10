/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: Production System Configuration Manager (PSCM)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the AMS (Asset Management System), which has been developed
 * at the PDE department of the FZI, Karlsruhe. It is part of the SkillPro Framework,
 * which is is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733).
 *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework. If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

package aml.skillpro.transformation.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.reflections.util.ClasspathHelper;

import transformation.interfaces.ITransformable;
import aml.skillpro.transformation.adapters.FactoryNodeAdapter;

import com.eztech.util.JavaClassFinder;

public class TransformationUtil {
	private final static TransformationUtil INSTANCE = new TransformationUtil();
	
	/**
	 * FIXME find another method to do this without using constants
	 * This has to always be updated each time the package name is changed
	 */	
	private static final String AML_TRANSFORMATION_ADAPTERS_PACKAGE_NAME = "aml.skillpro.transformation.adapters";
	/**
	 * FIXME find another method to do this without using constants
	 * This has to always be updated each time the package name is changed
	 */
	private static final String AML_TRANSFORMATION_INTERFACES_PACKAGE_NAME = "aml.skillpro.transformation.interfaces";
	
	private Set<Class<?>> transformables;
	private Map<Class<?>, Set<Class<?>>> implementedTransformablesMap = new HashMap<>(); 
	
	private TransformationUtil() {
	}
	
	public static TransformationUtil getInstance() {
		return INSTANCE;
	}
	
	public static Set<Class<?>> getAllTransformables() {
		if (INSTANCE.transformables == null) {
			INSTANCE.transformables = INSTANCE
					.getAllClassesImplementingTransformable(AML_TRANSFORMATION_INTERFACES_PACKAGE_NAME,
							ITransformable.class.getClassLoader(), ITransformable.class);
		}
		return INSTANCE.transformables;
	}
	
	public static Set<Class<?>> getAllModelsImplementingTransformable(Class<? extends ITransformable> trans) {
		Set<Class<?>> classesImplementingTransformable = INSTANCE.implementedTransformablesMap.get(trans);
		if (classesImplementingTransformable == null) {
			classesImplementingTransformable = INSTANCE
					.getAllClassesImplementingTransformable(AML_TRANSFORMATION_ADAPTERS_PACKAGE_NAME,
							FactoryNodeAdapter.class.getClassLoader(), trans);
			INSTANCE.implementedTransformablesMap.put(trans, classesImplementingTransformable);
		}
		return classesImplementingTransformable;
	}
	
	private Set<Class<?>> getAllClassesImplementingTransformable(String packageName, ClassLoader loader, Class<?> trans) {
		Set<Class<?>> result = new HashSet<>();
		Class<?> toMatch = trans;
		Set<URL> forPackage = new HashSet<>();
		forPackage.addAll(ClasspathHelper.forPackage(packageName, loader));
		for (URL pkgURL : forPackage) {
			try {
				URL newURL = FileLocator.resolve(pkgURL);
				URL url = new URL(newURL.toString());
				String path = url.toString().replace("file:/", "");
				System.setProperty(JavaClassFinder.CUSTOM_CLASS_PATH_PROPERTY, path);
				JavaClassFinder classFinder = new JavaClassFinder();
				List<Class<?>> classes = new ArrayList<>();
				classes.addAll(classFinder.findAllMatchingTypes(toMatch, loader));
				for (Class<?> transformable : classes) {
					if (transformable.getName() != null && !transformable.getName().isEmpty()
							&& !transformable.getSimpleName().equalsIgnoreCase("itransformable")
							&& !transformable.getSimpleName().equalsIgnoreCase("ipropertytransformable")) {
						//won't add ITransformable.class and IPropertyTransformable
						result.add(transformable);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
