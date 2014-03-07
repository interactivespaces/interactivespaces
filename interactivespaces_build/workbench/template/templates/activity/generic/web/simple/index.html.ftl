<html>
  <head>
<#list project.extraTemplateEntries as entry>
    ${entry}
</#list>
    <script src=\"js/${webAppFileBase}.js\" type="text/javascript" />
  </head>
  <body>
    Hello World.
  </body>
</html>
