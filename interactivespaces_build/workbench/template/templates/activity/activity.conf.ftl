space.activity.type=${activity.activityType}
space.activity.executable=${spec.executable}

space.activity.name=${activity.activityRuntimeName}

<#list spec.extraConfigurationParameters as parameter>
${parameter.name}=${parameter.value}
</#list>
