$(function(){
    $(document).ready(function(){
        $('#search').on('keyup', function() {
            var searchText = $(this).val().toLowerCase();
            if (searchText === '') {
                $('.menu li').each(function(){
                    $(this).show();
                });
            } else {
                $('.menu li').each(function(){
                    $(this).hide();
                });

                $('.searchable').each(function(){
                    var words = searchText.replace(/[0-9]/, '').toLowerCase().split(" ");
                    var joinedWords = words.join(")(?=.*");
                    var regex = new RegExp("^(?=.*" + joinedWords + ").*$");
                    if (regex.test($(this).text().toLowerCase())) {
                        //console.log($(this).text());
                        if ($(this).hasClass('subject')) {
                            $(this).parent().show();
                            $(this).siblings().children().show();
                            $(this).siblings().children().children().children().show();
                        } else if ($(this).hasClass('module')) {
                            $(this).parent().parent().parent().show();
                            $(this).parent().show();
                            $(this).siblings().children().show();
                        } else if ($(this).hasClass('course')) {
                            $(this).parent().parent().parent().parent().parent().parent().show();
                            $(this).parent().parent().parent().parent().show();
                            $(this).parent().parent().show();
                        }
                    }
                });
            }
        });
    });
});

$('.input_class_checkbox').each(function(){
    var check_div = '<div class="class_checkbox" />';
    if ($(this).is(':checked')) {
        check_div = '<div class="class_checkbox checked"/>';
    }
    $(this).hide().after(check_div);
});

$('.class_checkbox').on('click', function(){
    $('#bottombar').fadeIn(200);
    var div = $(this);
    div.toggleClass('checked');
    var checkbox = div.prev();
    checkbox.prop('checked', !div.is('.checked'));
});

$('.checklabel span').on('click', function(){
    $('#bottombar').fadeIn(200);
    var div = $(this).prev();
    div.toggleClass('checked');
    var checkbox = div.prev();
    checkbox.prop('checked', !div.is('.checked'));
});