# NovenaInjector
A java JarFile dependency injector agent, because I hate big .jar files :D

## How to use
1. [Download](https://github.com/Lucky-Development-Department/NovenaInjector/releases) the java agent file.
2. Add the java agent to your dependency (no need to shade it)
3. Write the injection method
4. Add the java agent to your server directory
5. Add `-javaagent:NovenaInjector.jar` to your JVM Flag
6. Start the server
7. ???
8. Profit!

## Methods
```java
NovenaInjector.appendJarFile(JarFile) #Injects 
```

### Method Example
```java
public void injectJar(File file) {
	if (file.getName().endsWith(".jar") {
		JarFile jarFile = new JarFile(file);
		NovenaInjector.appendJarFile(jarFile);
	}
}
```

#### Credits
[Stackoverflow](https://stackoverflow.com/a/52741647)
