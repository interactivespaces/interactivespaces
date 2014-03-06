<#ftl strip_whitespace=true>
space.activity.type=${project.activityType}
space.activity.executable=${project.activityExecutable}
<#if project.activityClass?has_content>
space.activity.java.class=${project.activityClass}
</#if>

space.activity.name=${project.activityRuntimeName}

<#list project.extraConfigurationParameters as parameter>
${parameter.name}=${parameter.value}
</#list>
<#list project.configurationProperties as parameter>
${parameter.name}=${parameter.value}
</#list>
