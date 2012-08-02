<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="src" path="src/main/java"/>
	<classpathentry kind="src" path="src/main/resources"/>
	<classpathentry exported="true" kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>

<#list libs as lib>
	<classpathentry kind="lib" path="${lib}"/>
</#list>

	<classpathentry kind="output" path="bin"/>
</classpath>
