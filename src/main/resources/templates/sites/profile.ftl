<#-- @ftlvariable name="user" type="types.User" -->
<form action="/profile" method="post">
    <table id="courses">
        <tr>
            <td>Your ID</td>
            <td>${user.userId?c}</td>
        </tr>
        <tr>
            <td>Name</td>
            <td><input type="text" name="fullName" value="${user.fullName!""}"></td>
        </tr>
        <tr>
            <td>Current Semester</td>
            <td><input type="number" name="currentSemester" min="0" max="20" value="${user.currentSemester!"0"}"></td>
        </tr>
        <#--<tr>
            <td>Mayor</td>
            <td>
                <label>
                    <select name="mayor">
                        <option value="SoftwareEngineering" <#if user.mayor == "SoftwareEngineering">checked</#if>>Software Engineering</option>
                        <option value="TechnischeInformatik" <#if user.mayor == "TechnischeInformatik">checked</#if>>Technische Informatik</option>
                        <option value="MedienInformatik" <#if user.mayor == "MedienInformatik">checked</#if>>Medien Informatik</option>
                        <option value="MedizinischeInformatik" <#if user.mayor == "MedizinischeInformatik">checked</#if>>Medizinische Informatik</option>
                        <option value="Wirtschaftsinformatik" <#if user.mayor == "Wirtschaftsinformatik">checked</#if>>Wirtschaftsinformatik</option>
                    </select>
                </label>
            </td>
        </tr>-->
    </table>
    <div id="bottombar">
        <input type="submit" value="Update">
    </div>
</form>