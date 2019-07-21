<#-- @ftlvariable name="subjects" type="java.util.Map<String, java.util.Map<java.lang.String, java.util.List<types.Course>>>" -->
<div id="courses-list">
    <div class="text">
        <small>Hier kannst du deine Kurse abonnieren. Bitte beachte, dass Channels erst erstellt werden, nachdem sich mindestens zwei Studierende für einen Kurs interessieren. Außerdem kann es etwas dauern, bis die Channels auf Discord für dich sichtbar werden!</small>
    </div>
    <input type="text" id="search" class="inputfield" placeholder="Search" />
    <form action="/courses" method="post">
        <#--<span class="sort" data-sort="subject">Sort by subject</span>-->
        <ul class="menu">
            <#list subjects as subject, modules>
                <li>
                    <span class="searchable subject disHighlight">${(subject)!""}</span>
                    <ul class="module-list">
                        <#list modules as module, courses>
                            <li class="blocked">
                                <span class="searchable module disHighlight">${(module)!""}</span>
                                <ul>
                                    <#list courses as course>
                                        <li>
                                            <label class="checklabel">
                                                <input type="checkbox" name="${course.courseId}" class="input_class_checkbox" <#if course.enabled>checked</#if>>
                                                <span class="searchable course disHighlight">${(course.course)!""} <small>${(course.shorthand)!""}</small></span>
                                            </label>
                                        </li>
                                    </#list>
                                </ul>
                            </li>
                        </#list>
                    </ul>
                </li>
            </#list>
        </ul>
        <div id="bottombar">
            <img src="static/check.svg">
            <input type="submit" value="Apply Changes">
        </div>
    </form>
</div>
<script src="/static/jquery.js"></script>
<script src="/static/courses.js"></script>
<script>
    var clicked = false;
    $('#bottombar').on('click', function(event){
        if (!clicked) {
            if (!$(event.target).is("#bottombar input")) {
                $("#bottombar input").trigger('click');
                clicked = true
            }
        }
    });
</script>
