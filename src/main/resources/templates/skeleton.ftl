<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->
<html lang="en">
<head>
    <title>${title}</title>
    <#--<link rel="stylesheet" href="/static/style.css" type="text/css">-->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="/static/courses.js"></script>
    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400" rel="stylesheet">
    <link rel="stylesheet" href="/static/courses.css">
    <link rel="stylesheet" href="/static/index.css">
    <link rel="stylesheet" href="/static/discordInvite.css">
</head>
<body>
<nav><#include "header.ftl"></nav>
<main>${content}</main>
</body>
</html>
