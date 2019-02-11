<#-- @ftlvariable name="course" type="types.Course" -->
<#-- @ftlvariable name="channels" type="java.util.Set<types.Channel>" -->
<form id="form-info" action="/admin/courses/${course.courseId}/update-info" method="post">
    <table>
        <tr>
            <td class="gray">Name</td>
            <td><input type="text" name="course" placeholder="${course.course}"></td>
        </tr>
        <tr>
            <td class="gray">Module</td>
            <td><input type="text" name="module" placeholder="${course.module!"<None>"}"></td>
        </tr>
        <tr>
            <td class="gray">Subject</td>
            <td><input type="text" name="subject" placeholder="${course.subject}"></td>
        </tr>
        <tr>
            <td class="gray">Shorthand</td>
            <td><input type="text" name="shorthand" placeholder="${course.shorthand!"<None>"}"></td>
        </tr>
    </table>
    <div class="sideOut">
        <input type="submit" style="margin: 20px 0;" class="button2" value="Update Info">
    </div>
</form>
<form id="form-channels" action="/admin/courses/${course.courseId}/update-channels" method="post">
    <table>
        <tr>
            <td class="gray">Channels</td>
            <td>
                <ul>
                    <#list channels as channel>
                        <li>
                            <input type="text" name="${channel.channelId?c}" placeholder="${channel.name}">
                        </li>
                    </#list>
                </ul>
            </td>
        </tr>
    </table>
    <div class="sideOut">
        <span id="addChannel" class="button2">Add Channel</span>
        <input type="submit" class="button2" value="Update Channels">
    </div>
</form>
<script src="/static/jquery.js"></script>
<script>
    $("#addChannel").click(function() {
        $('<form action="/admin/courses/${course.courseId}/add-channel" method="post"></form>')
            .appendTo($(document.body))
            .submit();
    });
</script>