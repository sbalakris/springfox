$(function() {
    function baseUrl() {
        var urlMatches =  /(.*)\/swagger-ui.html.*/.exec(window.location.href);
        return urlMatches[1];
    }

    $('#select_baseUrl').change(function () {
        window.swaggerUi.headerView.trigger('update-swagger-ui', {
            url: swaggerDropdown.val()
        });
    });

    $(document).ready(function() {
        var relativeLocation = baseUrl();

        $('#input_baseUrl').hide();

        $.getJSON(relativeLocation + "/swagger-resources", function(data) {

            var $urlDropdown = $('#select_baseUrl');
            $urlDropdown.empty();
            $.each(data, function(i, resource) {
                var option = $('<option></option>')
                        .attr("value", relativeLocation + resource.location)
                        .text(resource.name + " (" + resource.location + ")");
                $urlDropdown.append(option);
            });
            $urlDropdown.change();
        });
    });

});


