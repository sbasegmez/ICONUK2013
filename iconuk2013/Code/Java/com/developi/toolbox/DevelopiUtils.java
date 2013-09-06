package com.developi.toolbox;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public final class DevelopiUtils {

	public static int NAMES_READER=1;
	public static int NAMES_AUTHOR=2;
	
	/**
	 * recycles a domino document instance
	 * 
	 * @param lotus.domino.Base 
	 *           obj to recycle
	 * @category Domino
	 * @author Sven Hasselbach
	 * @category Tools
	 * @version 1.1
	 */
	public static void recycleObject(lotus.domino.Base obj) {
		if (obj != null) {
			try {
				obj.recycle();
			} catch (Exception e) {}
		}
	}

	/**
	 * 	 recycles multiple domino objects (thx Nathan T. Freeman)
	 *		
	 * @param objs
	 * 
	 */
	public static void recycleObjects(lotus.domino.Base... objs) {
		for ( lotus.domino.Base obj : objs ) 
			recycleObject(obj);
	}

	/**
	 * Converts any collection to a vector.
	 * 
	 * @author sbasegmez
	 * 
	 * @param collection
	 * @return
	 */
	

	@SuppressWarnings("unchecked")
	public static Vector<?> toVector(Collection<?> collection) {
		Vector v=new Vector();
		v.addAll(collection);
		return v;
	}
	
	/**
	 * Make any list unique
	 *
	 * @author sbasegmez
	 * @param list
	 * @return
	 */
	
	public static <T> List<T> uniqueList(List<T> list) {
		Vector<T> v=new Vector<T>();
		for(T obj: list ) {
			if(! v.contains(obj)) {
				v.add((T) obj);
			}
		}
		return v;
	}

	/**
	 * Calculates number of dates between two Date values.
	 * 
	 * @author sbasegmez
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */

	 public static int daysBetween(Date d1, Date d2){
         return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	 }
	
	
}
