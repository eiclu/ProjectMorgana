<#-- @ftlvariable name="navigation" type="java.util.Set<web.Page>" -->
<#-- @ftlvariable name="user" type="types.User" -->
<ul>

    <#list navigation as page>
        <li>
            <a href="${page.url!"#"}" class="<#if !page.url??>enabled</#if>">
                <#if page.iconName??>
                    <svg class="feather">
                        <use xlink:href="static/feather-sprite.svg#${page.iconName}"/>
                    </svg>
                </#if>
                <span>${page.title}</span>
            </a>
        </li>
    </#list>
    <#if user??>
        <li id="username">
            <ul>
                <li class="name">${user.userName!""}</li>
                <li class="tag">${user.userTag!""}</li>
                <li class="logout">
                    <a href="/logout">Logout</a>
                </li>
            </ul>
            <#if user.userImageUrl??>
                <img src="${user.userImageUrl}">
            </#if>
        </li>
    </#if>
</ul>