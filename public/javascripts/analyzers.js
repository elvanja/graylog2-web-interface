$(document).ready(function() {

    $(".open-analyze-field").on("click", function() {
        $(".analyze-field", $(this).parent()).toggle();
        $(this).toggleClass("open-analyze-field-active");
    });

    $(".analyze-field .generate-statistics").on("click", function() {
        var container = $(this).parent();
        $(this).attr("disabled", "disabled");

        showStatistics($(this).attr("data-field"), container);
    })

    function showStatistics(field, container) {
        var statistics = $(".statistics", container);

        $.ajax({
            url: '/a/search/fieldstats',
            data: {
                "timerange": originalSearchTimerange(),
                "q": originalSearchQuery(),
                "field": field
            },
            success: function(data) {
                statistics.show();
                $(".analyzer-content", container).show();
                $("dd.count", statistics).text(data.count);
                $("dd.mean", statistics).text(data.mean.toFixed(2));
                $("dd.stddev", statistics).text(data.std_deviation.toFixed(2));
                $("dd.min", statistics).text(data.min);
                $("dd.max", statistics).text(data.max);
                $("dd.sum", statistics).text(data.sum.toFixed(2));
                $("dd.variance", statistics).text(data.variance.toFixed(2));
                $("dd.squares", statistics).text(data.sum_of_squares.toFixed(2));
            },
            statusCode: { 400: function() {
                $(".wrong-type", statistics).show();
                statistics.show();
            }},
            error: function(data) {
               if(data.status != 400) {
                    statistics.hide();
                    showError("Could not load field statistics.");
               }
            },
            complete: function() {
                $(".spinner", container).hide();
            }
        });
    }

});