<#ftl strip_whitespace=true>
space.activity.type=${project.activityType}
space.activity.executable=${project.activityExecutable}
<#if project.activityRuntimeName?has_content>
space.activity.java.class=${project.activityRuntimeName}
</#if>

space.activity.name=${project.activityName}

<#list project.extraConfigurationParameters as parameter>
${parameter.name}=${parameter.value}
</#list>
<#list project.configurationProperties as parameter>
${parameter.name}=${parameter.value}
</#list>
