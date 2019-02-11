<#-- @ftlvariable name="location" type="java.lang.String" -->
<#-- @ftlvariable name="delay" type="int" -->
<script>
    setTimeout(function(){
        window.location.href = "${location}";
    }, ${delay});
</script>