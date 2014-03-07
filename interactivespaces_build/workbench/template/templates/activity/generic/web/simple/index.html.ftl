<html>
  <head>
<#list project.extraTemplateEntries as entry>
    ${entry}
</#list>
    <link rel="stylesheet" href="css/${webAppFileBase}.css" type="text/css" />
    <script src="js/${webAppFileBase}.js" type="text/javascript"></script>
  </head>
  <body>
    Hello World.
  </body>
</html>
