<html>
<#if project.extraTemplateEntries?has_content>
<#list project.extraTemplateEntries as entry>
    ${entry}
</#list>
</#if>
${webAppFileBase}
</html>
