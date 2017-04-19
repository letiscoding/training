/**
 * Created by lihui on 2017/3/27.
 */
$(function(){
    $(".jumbotron").hide();
    $("#btnAdd").click(function () {
        $("#dataTable tbody").append($("#dataTable tr:last"));
    })
    $("#btnSave").click(function () {
        var all = "";
        $("#dataTable tbody").find("tr").each(function () {
            var websitName = $(this).children('td').eq(0).find("input").val();
            var sourceUrl = $(this).children('td').eq(1).find("input").val();
            var channelUrlRegex = $(this).children('td').eq(2).find("input").val();
            var contentRegx = $(this).children('td').eq(3).find("input").val();
            var contentTitleRegx = $(this).children('td').eq(4).find("input").val();
            all+="{websitName:\""+websitName+"\",sourceUrl:\""+sourceUrl+"\",channelUrlRegex:\""+channelUrlRegex+"\",contentRegx:\""+contentRegx+"\",contentTitleRegx:\""+contentTitleRegx+"\"}"
            all+=","
        })
        alert(all);
        all=all.substr(0,all.length-1)
        all="{"+all+"}"
        $.post("/save",
            { all: all },
            function (data) {
                var jsonData = eval(data);
                if(jsonData.code!=200) {
                    alert(data.body);
                    $(obj).show();
                    return;
                }
                var txt = "<a href='book/"+bookName+".txt'>"+bookName+"</a>";
                $(obj).parent().append(txt);

            },
            "json");
    })
})


function deleteRow(obj) {
    $(obj).parent().remove();
}
