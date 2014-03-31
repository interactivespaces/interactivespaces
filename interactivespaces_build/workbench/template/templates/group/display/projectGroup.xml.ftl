<projectGroup>
  <name>${group.name}</name>
  <description>
  ${group.description}
  </description>

  <version>${group.version}</version>
  <groupName>${groupName}</groupName>

<#if group.projectList?has_content>
  <projects>
      <#list group.projectList as project>
          <#assign attributes="">
          <#list project.attributes?keys as key>
              <#if project.attributes[key]?has_content><#assign attributes="${attributes} ${key}=\"${project.attributes[key]}\""></#if>
          </#list>
        <project${attributes}/>
      </#list>
  </projects>
</#if>

</projectGroup>
