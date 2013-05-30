<?xml version="1.0" encoding="UTF-8"?>
<classpath>

<#list srcs as src>
	<classpathentry kind="src" path="${src}"/>
</#list>
	<classpathentry exported="true" kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>

<#list libs as lib>
	<classpathentry kind="lib" path="${lib}"/>
</#list>

	<classpathentry kind="output" path="bin"/>
</classpath>
