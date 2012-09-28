space.activity.type=${spec.project.activityType}
space.activity.executable=${spec.executable}

space.activity.name=${spec.project.activityDescription..activityRuntimeName}

<#list spec.extraConfigurationParameters as parameter>
${parameter.name}=${parameter.value}
</#list>
