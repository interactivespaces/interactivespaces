<?xml version="1.0"?>
<project type="${project.type}" <#if project.builderType?has_content>builder="${project.builderType}"</#if>>
  <name>${project.name}</name>
  <description>
${project.description}
  </description>

  <identifyingName>${project.identifyingName}</identifyingName>
  <version>${project.version}</version>
<#if project.sources?has_content>

  <sources>
<#list project.sources as source>
    ${source.toXml()}
</#list>
  </sources>
</#if>
<#if project.resources?has_content>

  <resources>
<#list project.resources as resource>
    ${resource.toXml()}
</#list>
  </resources>
</#if>
<#if project.type == 'activity'>

  <activity type="${project.activityType}">
    <name>${project.activityRuntimeName}</name>
<#if project.activityExecutable?has_content>
    <executable>${project.activityExecutable}</executable>
</#if>
<#if project.activityClass?has_content>
    <class>${project.activityClass}</class>
</#if>
<#if project.configurationProperties?has_content>

    <configuration>
<#list project.configurationProperties as property>
      <property name="${property.name}" required="${property.required?string}">
<#if property.descripton?has_content>
        <description>${property.description}</description>
</#if>
        <value>${property.value}</value>
      </property>
</#list>
    </configuration>
</#if>
  </activity>
</#if>
</project>
