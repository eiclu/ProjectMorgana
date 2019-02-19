<#-- @ftlvariable name="subjects" type="java.util.Map<String, java.util.Map<java.lang.String, java.util.List<types.Course>>>" -->
<p>Hier kannst du deine Kurse abonnieren. Bitte beachte, dass Channels erst erstellt werden, nachdem sich mindestens zwei Studierende für einen Kurs interessieren. </p>
<p class="warn">Zurzeit kann es etwas länger dauern, bis die Channels freigeschalten werden, wir bitten um Geduld.</p>
<input type="text" id="search" placeholder="Search">
<dl class="subjects">
    <#list subjects as subject, modules>
        <dt class="searchable">${subject!""}</dt>
        <dd>
            <dl class="modules">
                <#list modules as module, courses>
                    <dt class="searchable">${module!""}</dt>
                    <dd>
                        <ul>
                            <#list courses as course>
                                <li>
                                    <label class="course">
                                        <input type="checkbox" name="${course.courseId}">
                                        <span class="searchable breakable">${course.course} <small>${course.shorthand!""}</small></span>
                                    </label>
                                </li>
                            </#list>
                        </ul>
                    </dd>
                </#list>
            </dl>
        </dd>
    </#list>
</dl>