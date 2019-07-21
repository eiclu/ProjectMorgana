<#-- @ftlvariable name="subjects" type="java.util.Map<String, java.util.Map<java.lang.String, java.util.List<types.Course>>>" -->
<div id="addChannelTable">
    <form action="/admin/addCourse" method="post">
        <ul>
            <li><input type="text" placeholder="Subject" name="subject"></li>
            <li><input type="text" placeholder="Module" name="module"></li>
            <li><input type="text" placeholder="Course" name="course"></li>
            <li><input type="text" placeholder="Shorthand" name="shorthand"></li>
        </ul>
        <input type="submit" class="button2 sideOut" value="Add Course">
    </form>
</div>
<div id="courses-list">
    <input type="text" class="inputfield" id="search" placeholder="Search" />
    <form action="/admin/deleteCourses" method="post">
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
                                                <input type="checkbox" name="${course.courseId}" class="input_class_checkbox" >
                                                <span  class="searchable course disHighlight"><a href="/admin/courses/${course.courseId}">${(course.course)!"<None>"}</a> <small>${(course.shorthand)!""}</small> (${course.userCount})</span>
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
        <div id="bottombar" style="background-color: rgba(120,2,17,0.92);">
            <span>Delete all selected</span>
            <input style="display: none;" type="submit" value="Are you sure?">
        </div>
    </form>
</div>
<script src="/static/jquery.js"></script>
<script>
    var second = false;
    var clicked = false;
    $('#bottombar').on('click', function(event){
        if (!second) {
            $('#bottombar span').delay(1000).hide();
            $("#bottombar input").delay(1000).fadeIn(200);
            second = true
        } else if (!clicked && !$(event.target).is("#bottombar input")) {
            $("#bottombar input").trigger('click');
            clicked = true;
        }
    });

    $('.input_class_checkbox').each(function(){
        $(this).prop('checked', false);
    });
</script>
<script src="/static/courses.js"></script>
