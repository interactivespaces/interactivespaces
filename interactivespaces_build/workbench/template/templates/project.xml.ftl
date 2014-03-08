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
    <#if source.sourceDirectory?has_content><#assign srcAttr="sourceDirectory=\"${source.sourceDirectory}\""></#if>
    <#if source.sourceFile?has_content><#assign srcAttr="sourceFile=\"${source.sourceFile}\""></#if>
    <#if source.destinationDirectory?has_content><#assign dstAttr="destinationDirectory=\"${source.destinationDirectory}\""></#if>
    <#if source.destinationFile?has_content><#assign dstAttr="destinationFile=\"${source.destinationFile}\""></#if>
    <${source.getTypeName()} ${srcAttr!""} ${dstAttr!""} />
</#list>
  </sources>
</#if>
<#if project.resources?has_content>

  <resources>
<#list project.resources as resource>
<#if resource.sourceDirectory?has_content><#assign srcAttr="sourceDirectory=\"${resource.sourceDirectory}\""></#if>
<#if resource.sourceFile?has_content><#assign srcAttr="sourceFile=\"${resource.sourceFile}\""></#if>
<#if resource.destinationDirectory?has_content><#assign dstAttr="destinationDirectory=\"${resource.destinationDirectory}\""></#if>
<#if resource.destinationFile?has_content><#assign dstAttr="destinationFile=\"${resource.destinationFile}\""></#if>
    <${resource.getTypeName()} ${srcAttr!""} ${dstAttr!""} />
</#list>
  </resources>
</#if>
<#if project.dependencies?has_content>

  <dependencies>
<#list project.dependencies as dependency>
    <dependency name="${dependency.name}" required="${dependency.required?string}" minimumVersion="${dependency.minimumVersion}" maximumVersion="${dependency.maximumVersion}" />
</#list>
  </dependencies>
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
