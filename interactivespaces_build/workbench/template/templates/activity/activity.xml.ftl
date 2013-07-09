<?xml version="1.0"?>
<activity <#if project.builderType?has_content>builder="${project.builderType}"</#if>>
    <name>${project.name}</name>
    <description>
${project.description}
    </description>

    <identifyingName>${project.identifyingName}</identifyingName>
    <version>${project.version}</version>

<#if project.dependencies?has_content>    <dependencies><#list project.dependencies as dependency>
        <dependency name="${dependency.name}"
<#if dependency.minimumVersion?has_content>          minimumVersion="${dependency.minimumVersion}"</#if>
<#if dependency.maximumVersion?has_content>          maximumVersion="${dependency.maximumVersion}"</#if>
          required="<#if dependency.required>true<#else>false</#if>" />
</#list>    </dependencies></#if>
</activity>
