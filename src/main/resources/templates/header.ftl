<#-- @ftlvariable name="navigation" type="java.util.Set<web.Page>" -->
<#-- @ftlvariable name="user" type="types.User" -->
<div id="nav">
    <ul>
        <#list navigation as page>
            <li class="<#if !page.url??>active</#if>">
                <#if !page.url??><b><#else><a href="${page.url}"></#if>
                    <img src="${page.iconUrl!"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"}">
                    <p>${page.title}</p>
                <#if !page.url??></b><#else></a></#if>
            </li>
        </#list>
    </ul>
    <#if user??>
        <div id="username">
            <div>
                <p class="user-name">${user.userName!""}</p>
                <p class="user-tag">${user.userTag!""}</p>
                <p class="user-logout"><a href="/logout">Logout</a></p>
            </div>
            <#if user.userImageUrl??>
                <img src="${user.userImageUrl}">
            </#if>
        </div>
    </#if>
</div>