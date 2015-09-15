## Overview

国际化i18n处理

## Usage

```java

com/messages.properties
   key1 = Hello world
   key2 = Hello, {1}, welcome to {2}
   key3 = Hello, {a}, welcome to {b}

ResourceBundleLocator locator = ResourceBundleLocator.getLocator("com.messages");
String msg1 = locator.getString("key1"); 
// msg1 -- Hello world

String msg2 = locator.getString("key2", "Jack", "Java");
// msg2 -- Hello, Jack, welcome to Java

Map<String, String> paramMap = new HashMap<String, String>();
paramMap.put("a", "Jack");
paramMap.put("b", "Java");

String msg3 = locator.getString("key3", paramMap);
// msg3 -- Hello, Jack, welcome to Java

```

