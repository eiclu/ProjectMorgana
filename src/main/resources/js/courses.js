
$(document).on("click", ".course",function(){
    let input = $(this).find("input").eq(0);
    console.log("Button: " + input.attr("name") + " checked: " + input.is(':checked'))
});

$(document).on("keyup", "#search", function() {
    var searchText = $(this).val().replace(/[0-9]/, "").toLowerCase();
    if (searchText === "") {
        $("main li, main dt, main dd").each(function(){
            $(this).show();
        });
    } else {
        $("main li, main dt, main dd").each(function(){
            $(this).hide();
        });

        $(".searchable").each(function(){
            var words = searchText.split(" ");
            var joinedWords = words.join(")(?=.*");
            var regex = new RegExp("^(?=.*" + joinedWords + ").*$");
            if (regex.test($(this).text().toLowerCase())) {
                if ($(this).parent().hasClass("subjects")) {
                    $(this).show();
                    $(this).next().show();
                    $(this).next().children().children().show();
                    $(this).next().children().children().children().children().show();
                } else if ($(this).parent().hasClass("modules")) {
                    $(this).parent().parent().prev().show();
                    $(this).parent().parent().show();
                    $(this).show();
                    $(this).next().show();
                    $(this).next().children().children().show();
                } else if ($(this).parent().hasClass("course")) {
                    $(this).parent().parent().parent().parent().parent().parent().prev().show();
                    $(this).parent().parent().parent().parent().parent().parent().show();
                    $(this).parent().parent().parent().parent().prev().show();
                    $(this).parent().parent().parent().parent().show();
                    $(this).parent().parent().show();
                }
            }
        });
    }
});