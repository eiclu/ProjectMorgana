<#-- @ftlvariable name="user" type="types.User" -->
<#-- @ftlvariable name="majors" type="java.util.List<types.Major>" -->
<form action="/profile" method="post">
    <span class="subject disHighlight">${user.userName!""}${user.userTag!""}</span>
    <table id="courses">
        <tr>
            <td><label for="currentSemester">Current Semester</label></td>
            <td><input type="number" class="inputfield" name="currentSemester" id="currentSemester" min="1" max="20" value="${user.currentSemester!"1"}"></td>
        </tr>
        <tr>
            <td><label for="major">Major</label></td>
            <td>
                <select name="major" id="major" class="inputfield">
                    <#list majors as major>
                        <option value="${major!""}" <#if (user.mayor??) && (major == user.mayor)>selected</#if>>
                            ${major.majorName!""}
                        </option>
                    </#list>
                </select>
            </td>
        </tr>
    </table>
    <div class="sideOut">
        <input type="submit" class="button2" value="Update">
    </div>
</form>
