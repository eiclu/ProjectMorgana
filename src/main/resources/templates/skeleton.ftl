<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->
<html lang="en">
<head>
    <title>${title}</title>
    <link rel="stylesheet" href="/static/style.css" type="text/css">    
    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400" rel="stylesheet">
    <link rel="stylesheet" href="/static/discordInvite.css"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
<div id="header"><#include "header.ftl"></div>
<div id="content">${content}</div>
<div id="footer"><#include "footer.ftl"></div>
</body>
</html>
