/**
 * Used by the web methods.
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */
// 获取所有的Web元素
function allWebElements() {
	for (var key in document.all){
		try{
		// 通过Robotium WebClient
			promptElement(document.all[key]);			
		}catch(ignored){}
	}
	// 通知脚本执行完毕
	finished();
}
// 遍历所有 TEXT节点
function allTexts() {
//初始化一个range
	var range = document.createRange();
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,null,false); 
	while(n=walk.nextNode()){
		try{
		// 通知Robotium WebClient
			promptText(n, range);
		}catch(ignored){}
	} 
	// 通知脚本执行完毕
	finished();
}

// 点击Element
function clickElement(element){
    // 构造点击动作事件
	var e = document.createEvent('MouseEvents');
	e.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
	element.dispatchEvent(e);
}
//查找指定id的element,click为true则点击,为false则告诉Robotium WebClient相关信息
function id(id, click) {
    // 获取id对应的 Element
	var element = document.getElementById(id);
	// 找到Element则发送点击
	if(element != null){ 
	// true则点击
		if(click == 'true'){
			clickElement(element);
		}
	// 否则告诉Robotium WebClient相关信息
		else{
			promptElement(element);
		}
	} 
	// 按照id未找到则遍历所有元素查询
	else {
		for (var key in document.all){
			try{
				element = document.all[key];
				// 找到则执行后续操作
				if(element.id == id) {
				// 为true则点击
					if(click == 'true'){
						clickElement(element);
						return;
					}
				// 其他则告知 robotiumWebClient相关信息
					else{
						promptElement(element);
					}
				}
			} catch(ignored){}			
		}
	}
	// js执行完毕
	finished(); 
}
// 按照xpath查找相关elements,click为true则点击查找到的第一个，非true则提交elements相关信息给RobotiumWebClient
function xpath(xpath, click) {
	// 按照xpath查找指定elements
	var elements = document.evaluate(xpath, document, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null); 

	if (elements){
	// 遍历elements
		var element = elements.iterateNext();
		while(element) {
			// 为true则点击
			if(click == 'true'){
				clickElement(element);
				return;
			}
			// 其他则提交相关信息给RobotiumWebClient
			else{
				promptElement(element);
				element = result.iterateNext();
			}
		}
		// 脚本执行结束
		finished();
	}
}
// 按照css查找相关elements,click为 true则点击找到的第一个，否则提交相关elements信息给RobotiumWebClient
function cssSelector(cssSelector, click) {
// 按照css查找相关elements
	var elements = document.querySelectorAll(cssSelector);
	// 遍历elements
	for (var key in elements) {
		if(elements != null){ 
			try{
			// click为 true则点击，并退出
				if(click == 'true'){
					clickElement(elements[key]);
					return;
				}
				//提交element相关信息给RobotiumWebClient
				else{
					promptElement(elements[key]);
				}	
			}catch(ignored){}  
		}
	}
	// 脚本执行结束
	finished(); 
}
// 按照name查找对应的element.click为true则点击遇到的第一个，否则提交element信息给RobotiumWebClient
function name(name, click) {
    // 获取遍历实例
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_ELEMENT,null,false); 
	// 遍历
	while(n=walk.nextNode()){
		try{
		// 检查是否是指定的name
			var attributeName = n.getAttribute('name');
			if(attributeName != null && attributeName.trim().length>0 && attributeName == name){
			// click为 true则点击，并退出
				if(click == 'true'){
					clickElement(n);
					return;
				}
				//提交element相关信息给RobotiumWebClient
				else{
					promptElement(n);
				}	
			}
		}catch(ignored){} 
	} 
	// 脚本执行结束
	finished();
}
// 按照classname查找element,click为true则点击遇到的第一个，否则提交element信息给RobotiumWebClient
function className(nameOfClass, click) {
// 获取遍历实例
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_ELEMENT,null,false); 
	// 遍历
	while(n=walk.nextNode()){
		try{
			var className = n.className; 
			// 找到对应的element
			if(className != null && className.trim().length>0 && className == nameOfClass) {
			// click为 true则点击，并退出
				if(click == 'true'){
					clickElement(n);
					return;
				}
			//提交element相关信息给RobotiumWebClient	
				else{
					promptElement(n);
				}	
			}
		}catch(ignored){} 
	} 
	// 脚本执行结束
	finished(); 
}
// 按照text查找element,click为true则点击遇到的第一个，否则提交element信息给RobotiumWebClient
function textContent(text, click) {
	// 获取对应的遍历实例
	var range = document.createRange();
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,null,false); 
	// 遍历
	while(n=walk.nextNode()){ 
		try{
			var textContent = n.textContent; 
			// 找到指定的element
			if(textContent.trim() == text.trim()){  
			// click为 true则点击，并退出
				if(click == 'true'){
					clickElement(n);
					return;
				}
			//提交element相关信息给RobotiumWebClient	
				else{
					promptText(n, range);
				}
			}
		}catch(ignored){} 
	} 
	// 脚本执行结束
	finished();  
}
// 按照tagname查找element,click为true则点击遇到的第一个，否则提交element信息给RobotiumWebClient
function tagName(tagName, click) {
    // 查找对应的element
	var elements = document.getElementsByTagName(tagName);
	for (var key in elements) {
		if(elements != null){ 
			try{
			// click为 true则点击，并退出
				if(click == 'true'){
					clickElement(elements[key]);
					return;
				}
			//提交element相关信息给RobotiumWebClient	
				else{
					promptElement(elements[key]);
				}	
			}catch(ignored){}  
		}
	}
	// 脚本执行结束
	finished();
}
// 指定id的element设置text
function enterTextById(id, text) {
	var element = document.getElementById(id);
	if(element != null)
		element.value = text;

	finished(); 
}
// 指定xpath的element设置text
function enterTextByXpath(xpath, text) {
	// 只获取一个
	var element = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue;
	if(element != null)
		element.value = text;

	finished(); 
}
// 指定css的element设置text
function enterTextByCssSelector(cssSelector, text) {
	var element = document.querySelector(cssSelector);
	if(element != null)
		element.value = text;

	finished(); 
}
// 指定name的element设置text
function enterTextByName(name, text) {
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_ELEMENT,null,false); 
	while(n=walk.nextNode()){
		var attributeName = n.getAttribute('name');
		if(attributeName != null && attributeName.trim().length>0 && attributeName == name) 
			n.value=text;  
	} 
	finished();
}
// 指定classname的element设置text,参数名字写成className较好
function enterTextByClassName(name, text) {
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_ELEMENT,null,false); 
	while(n=walk.nextNode()){
		var className = n.className; 
		if(className != null && className.trim().length>0 && className == name) 
			n.value=text;
	}
	finished();
}
// 按照已有text内容查找对应的element并设置为指定的text
function enterTextByTextContent(textContent, text) {
	var walk=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,null,false); 
	while(n=walk.nextNode()){ 
		var textValue = n.textContent; 
		if(textValue == textContent) 
			n.parentNode.value = text; 
	}
	finished();
}
// 指定tagname的element设置text
function enterTextByTagName(tagName, text) {
	var elements = document.getElementsByTagName(tagName);
	if(elements != null){
		elements[0].value = text;
	}
	finished();
}
// 获取Element属性，并调用prompt方法，弹出属性,Robotium修改过的WebClient抓取这些信息，来构造页面元素 Element
function promptElement(element) {
    // 获取element的id
	var id = element.id;
	// 获取element的 text
	var text = element.innerText;
	// 过滤掉空格
	if(text.trim().length == 0){
		text = element.value;
	}
	// 获取element的name属性
	var name = element.getAttribute('name');
	// 获取element的classname属性
	var className = element.className;
	// 获取element的tagname属性
	var tagName = element.tagName;
	获取剩余的其他属性
	var attributes = "";
	var htmlAttributes = element.attributes;
	// 遍历剩余属性，通过#$分割属性
	for (var i = 0, htmlAttribute; htmlAttribute = htmlAttributes[i]; i++){
		attributes += htmlAttribute.name + "::" + htmlAttribute.value;
		if (i + 1 < htmlAttributes.length) {
			attributes += "#$";
		}
	}
	// 获取element大小,
	var rect = element.getBoundingClientRect();
	// 可见的element拼接字符串，传递给Robotium WebClient
	if(rect.width > 0 && rect.height > 0 && rect.left >= 0 && rect.top >= 0){
		prompt(id + ';,' + text + ';,' + name + ";," + className + ";," + tagName + ";," + rect.left + ';,' + rect.top + ';,' + rect.width + ';,' + rect.height + ';,' + attributes);
	}
}
// 按照range信息构造内容返回给Robotium WebClient
function promptText(element, range) {	
    // 获取Elemet的text内容
	var text = element.textContent;
	if(text.trim().length>0) {
	    // 设置range的范围为当前Element
		range.selectNodeContents(element);
		// 获取尺寸信息
		var rect = range.getBoundingClientRect();
		// 只返回可见的 Element
		if(rect.width > 0 && rect.height > 0 && rect.left >= 0 && rect.top >= 0){
			var id = element.parentNode.id;
			var name = element.parentNode.getAttribute('name');
			var className = element.parentNode.className;
			var tagName = element.parentNode.tagName;
			prompt(id + ';,' + text + ';,' + name + ";," + className + ";," + tagName + ";," + rect.left + ';,' + rect.top + ';,' + rect.width + ';,' + rect.height);
		}
	}
}

// js执行完毕，通知Robotium WebClient 完成了
function finished(){
	prompt('robotium-finished');
}
