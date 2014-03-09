package com.robotium.solo;

import java.lang.reflect.Field;

/**
 * 反射操作工具类
 * A reflection utility class.  
 * 
 * @author Per-Erik Bergman, bergman@uncle.se
 * 
 */

class Reflect {
	private Object object;

	/**
	 * 构造函数，禁止传入空值
	 * Constructs this object 
	 * 
	 * @param object the object to reflect on
	 */
	
	public Reflect(Object object) {
		// 传入空值报异常
		if (object == null)
			throw new IllegalArgumentException("Object can not be null.");
		this.object = object;
	}

	/**
	 * 获取对应属性字段
	 * Get a field from the object 
	 * 
	 * @param name the name of the field
	 * 
	 * @return a field reference
	 */
	
	public FieldRf field(String name) {
		return new FieldRf(object, name);
	}

	/**
	 * 定义一个字段属性类
	 * A field reference.  
	 */
	public class FieldRf {
		// 对应的 Class
		private Class<?> clazz;
		// 获取字段属性的对象
		private Object object;
		// 属性名
		private String name;

		/**构造函数
		 * 
		 * Constructs this object 
		 * 
		 * @param object the object to reflect on
		 * @param name the name of the field
		 */
		
		public FieldRf(Object object, String name) {
			this.object = object;
			this.name = name;
		}

		/**
		 * 构造指定class类型的对象
		 * Constructs this object 
		 * 
		 * @param outclazz the output type
		 *
		 * @return <T> T
		 */
		
		public <T> T out(Class<T> outclazz) {
			Field field = getField();
			Object obj = getValue(field);
			return outclazz.cast(obj);
		}

		/**
		 * 设置字段的值
		 * Set a value to a field 
		 * 
		 * @param value the value to set
		 */
		
		public void in(Object value) {
			Field field = getField();
			try {
				// 设置属性为传入的值
				field.set(object, value);
				// 无效参数异常
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				// 权限异常
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 设置class类型，并返回对象本身
		 * Set the class type 
		 * 
		 * @param clazz the type
		 *
		 * @return a field reference
		 */
		
		public FieldRf type(Class<?> clazz) {
			this.clazz = clazz;
			return this;
		}

		// 获取字段
		private Field getField() {
			//  如未设置class类型，那么使用对象自身class作为class类型
			if (clazz == null) {
				clazz = object.getClass();
			}
			// 获取name执行的属性字段
			Field field = null;
			try {
				field = clazz.getDeclaredField(name);
				// 字段属性设置为运行赋值
				field.setAccessible(true);
			} catch (NoSuchFieldException ignored) {}
			return field;
		}
		// 获取字段属性值
		private Object getValue(Field field) {
			// 如果字段为null那么返回null
			if (field == null) {
				return null;
			}
			// 获取字段对应的值，对象类型
			Object obj = null;
			try {
				obj = field.get(object);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return obj;
		}
	}

}
