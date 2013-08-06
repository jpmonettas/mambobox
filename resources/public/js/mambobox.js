/* Firing the file upload */
$(function () {
    $('#fileupload').fileupload({
        dataType: 'json',
        done: function (e, data) {
            $.each(data.result.files, function (index, file) {
                $('<p/>').text(file.name).appendTo(document.body);
            });
        },
	progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .bar').css(
		'width',
		progress + '%'
            );
	}
    });
});


/* Controlling the pagination buttons */
$(function (){
    $("ul.pagination li.page-link:not(.active) span").click(function(){
        var pageNumber=$(this).text();
        var curSearchString=document.location.search;
        var newSearch=insertParamSearch(curSearchString,"curpage",pageNumber);
        document.location.search=newSearch;
    });
    $("ul.pagination li:not(.disabled) span.left-arrow").click(function(){
        var pageNumber=parseInt($("ul.pagination li.active span").text());
        pageNumber--;
        var curSearchString=document.location.search;
        var newSearch=insertParamSearch(curSearchString,"curpage",pageNumber);
        document.location.search=newSearch;
    });
    $("ul.pagination li:not(.disabled) span.right-arrow").click(function(){
        var pageNumber=parseInt($("ul.pagination li.active span").text());
        pageNumber++;
        var curSearchString=document.location.search;
        var newSearch=insertParamSearch(curSearchString,"curpage",pageNumber);
        document.location.search=newSearch;
    });
});


/*Extracted from IB
 This simply replace/add a parameter in a qstring*/
function insertParamSearch(search, key, value)
{
  key = escape(key); value = escape(value);
  var kvp = search.substr(1).split('&');
  if (kvp == '') {
    return '?' + key + '=' + value;
  }
  else {
    var i=kvp.length; var x; while(i--) {
      x = kvp[i].split('=');
      if (x[0]==key){
        x[1] = value;
        kvp[i] = x.join('=');
        break;
      }
    }
    if(i<0) {kvp[kvp.length] = [key,value].join('=');}
    return '?' + kvp.join('&'); 
  }
}
