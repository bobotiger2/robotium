package com.robotium.solo;

import java.util.Hashtable;

/**
 * 定义WebView中各类元素，类似input之类的
 * Represents an element shown in a WebView.  
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

public class WebElement {
	// 对应屏幕中的该控件中间位置x坐标
	private int locationX = 0;
	// 对应屏幕中的改控件中间位置y坐标
	private int locationY = 0;
	// Web元素 id
	private String id;
	// Web元素text
	private String text;
	// Web元素name
	private String name;
	// Web元素class
	private String className;
	// Web元素tag
	private String tagName;
	// 其他额外属性
	private Hashtable<String, String> attributes;
	

	/**
	 * 构造函数
	 * Constructs this object. 
	 * 
	 * @param webId the given web id
	 * @param textContent the given text to be set
	 * @param name the given name to be set
	 * @param className the given class name to set
	 * @param tagName the given tag name to be set
	 * @param attributes the attributes to set
	 */

	public WebElement(String webId, String textContent, String name, String className, String tagName, Hashtable<String, String> attributes) {

		this.setId(webId);
		this.setTextContent(textContent);
		this.setName(name);
		this.setClassName(className);
		this.setTagName(tagName);
		this.setAttributes(attributes);
	}

	/**
	 * 获取 WebElement元素对应的屏幕坐标
	 * Returns the WebElements location on screen.
	 */

	public void getLocationOnScreen(int[] location) {

		location[0] = locationX;
		location[1] = locationY;
	}

	/**
	 * 设置屏幕相对x坐标
	 * Sets the X location.
	 * 
	 * @param locationX the X location of the {@code WebElement}
	 */

	public void setLocationX(int locationX){
		this.locationX = locationX;
	}

	/**
	 * 设置屏幕相对Y坐标
	 * Sets the Y location.
	 * 
	 * @param locationY the Y location of the {@code WebElement}
	 */

	public void setLocationY(int locationY){
		this.locationY = locationY;
	}

	/**
	 * 获取屏幕相对X坐标
	 * Returns the X location.
	 * 
	 * @return the X location
	 */

	public int getLocationX(){
		return this.locationX;
	}

	/**
	 * 获取屏幕相对Y坐标
	 * Returns the Y location.
	 * 
	 * @return the Y location
	 */

	public int getLocationY(){
		return this.locationY;
	}

	/**
	 * 获取id
	 * Returns the id.
	 * 
	 * @return the id
	 */

	public String getId() {
		return id;
	}

	/**
	 * 设置id
	 * Sets the id.
	 * 
	 * @param id the id to set
	 */

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取name
	 * Returns the name.
	 * 
	 * @return the name
	 */

	public String getName() {
		return name;
	}

	/**
	 * 设置name
	 * Sets the name.
	 * 
	 * @param name the name to set
	 */

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取class
	 * Returns the class name.
	 * 
	 * @return the class name
	 */

	public String getClassName() {
		return className;
	}

	/**
	 * 设置class
	 * Sets the class name.
	 * 
	 * @param className the class name to set
	 */

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * 获取tag
	 * Returns the tag name.
	 * 
	 * @return the tag name
	 */

	public String getTagName() {
		return tagName;
	}

	/**
	 * 设置tag
	 * Sets the tag name.
	 * 
	 * @param tagName the tag name to set
	 */

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * 获取text
	 * Returns the text content.
	 * 
	 * @return the text content
	 */

	public String getText() {
		return text;
	}

	/**
	 * 设置text
	 * Sets the text content.
	 * 
	 * @param textContent the text content to set
	 */
	
	public void setTextContent(String textContent) {
		this.text = textContent;
	}

	/**
	 * 获取凄然额外属性
	 * Returns the value for the specified attribute.
	 * 
	 * @return the value for the specified attribute
	 */

	public String getAttribute(String attributeName) {
		if (attributeName != null){
			return this.attributes.get(attributeName);
		}
		
		return null;
	}

	/**
	 * 设置额外属性
	 * Sets the attributes.
	 * 
	 * @param attributes the attributes to set
	 */
	
	public void setAttributes(Hashtable<String,String> attributes) {
		this.attributes = attributes;
	}

}
