/**
 * Created by lihui on 2017/3/27.
 */
$(function(){
    $(".jumbotron").hide();
    $("#search-button").click(function () {
        query(1);
    })

})
function loadTable(data) {
    if (data == null || data.length == 0) return;
    $("#booksTable tbody").html("");
    var bookName = $("#bookNameInput").val();
    for (var i = 0; i < data.length; i++) {
        var tr = "<tr>" +
        "<td>" +
        data[i].subject +
        "</td>" +
        "<td>" +
        data[i].webName +
        "</td>" +
        "<td>" +
        data[i].postTime +
        "</td>" +
        "<td>" +
        data[i].url +
        "</td>" +
        "<td>" +
        data[i].digest +
        "</td>" +
         "<td>"+
         "<input type='input' class='btn' value='下载' onclick='downLoad(this,"+"\""+data[i].url+"\""+")' >"+
         "</td>"+
        "</tr>";
        $("#booksTable tbody").append(tr);
    }
}
function downLoad(obj,bookUrl) {

    //var bookUrl ="";
    var bookName = $("#bookNameInput").val();
    var startTitle="";
    $(obj).hide();
    $.post("/down",
        { bookName: bookName,bookUrl:bookUrl,startTitle:startTitle },
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
}

function query(pageorder) {
    var bookName = $("#bookNameInput").val();
    $.get("/seach/"+bookName+"/"+pageorder,
        //{ Query: $("#inputQuery").val() },
        function (data) {
            var jsonData = eval(data);
            if(jsonData.code!=200) {
                alert(data.message);
                return;
            }
            loadTable(jsonData.body);
        },
        "json");
}