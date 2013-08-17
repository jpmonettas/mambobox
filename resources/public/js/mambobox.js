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

/* For controlling clicks on tags */
$(function (){
   $(".label.music-tag.search").click(function(){
        var tagName=$(this).text();
        $("#tag-filter").val(tagName);
        $(".search-section form").submit();
    });
});

/* For controlling clicks on search tags remove */
$(function (){
   $(".label.music-tag.remove").click(function(){
       $("#tag-filter").val("");
       $(".search-section form").submit();
    });
});

/* For removing a tag */
$(function (){
   $(".detail-tag li").click(function(){
       var tagElement=$(this);
       var tagName=$($(this).parent().siblings("span")[0]).text();
       var songId=$("#song-id").val();
       var deleteUrl='/music/' + songId + '/tags/' + tagName;
       $.ajax({
                type:"DELETE",
                url: deleteUrl,
                success:function(){
                   tagElement.remove(); 
                }
            });
    });
});

/* For removing a video link */
$(function (){
   $(".delete-video-button").click(function(){
       var videoWraperDiv=$(this).parent(".video-wrapper");
       var link=videoWraperDiv.find("input").val();
       var songId=$("#song-id").val();
       var deleteUrl='/music/' + songId + '/links/' + encodeURIComponent(link);
       $.ajax({
                type:"DELETE",
                url: deleteUrl,
                success:function(){
                   videoWraperDiv.remove(); 
                }
            });
    });
});


/* This simply replace/add a parameter in a qstring*/
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
 

$(document).ready(function (){
    /* For tag selection in the modal */
    $(".label-wrapper-div").click(function(){
        var labelHtml=$(this).html();
        $("#selected-tag").html(labelHtml);
    });
    $("#add-tag").click(function(){
        var songId=$("#song-id").val();
        var tagHtml=$("#selected-tag").html();
        var tagName=$("#selected-tag span").text();
        var postUrl="/music/" + songId + "/tags/" + tagName;
        if(tagName){
            $.ajax({
                type:"POST",
                url: postUrl,
                success:function(){
                    $("#main-music-detail-div .tags").prepend(tagHtml);
                    $('#select-tag-modal').modal('hide');
                }
            });
        }
    });

});


/* For the upload plugin */
$(function () {
    'use strict';
    // Change this to the location of your server-side upload handler:
    var url = "/upload";
    var uploadButton = $('<button/>')
            .addClass('btn btn-primary')
            .prop('disabled', true)
            .text('Processing...')
            .on('click', function () {
                var $this = $(this),
                    data = $this.data();
                $this
                    .off('click')
                    .text('Abort')
                    .on('click', function () {
                        $this.remove();
                        data.abort();
                    });
                data.submit().always(function () {
                    $this.remove();
                });
            });
    $('#fileupload').fileupload({
        url: url,
        dataType: 'json',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(mp3)$/i,
        maxFileSize: 10000000, // 10 MB
    }).on('fileuploadadd', function (e, data) {
        data.context = $('<div/>').appendTo('#files');
        $.each(data.files, function (index, file) {
            var node = $('<p/>')
                    .append($('<span/>').text(file.name));
            if (!index) {
                node
                    .append('<br>')
                    .append(uploadButton.clone(true).data(data));
            }
            node.appendTo(data.context);
        });
    }).on('fileuploadprocessalways', function (e, data) {
        var index = data.index,
            file = data.files[index],
            node = $(data.context.children()[index]);
        if (file.preview) {
            node
                .prepend('<br>')
                .prepend(file.preview);
        }
        if (file.error) {
            node
                .append('<br>')
                .append(file.error);
        }
        if (index + 1 === data.files.length) {
            data.context.find('button')
                .text('Upload')
                .prop('disabled', !!data.files.error);
        }
    }).on('fileuploadprogressall', function (e, data) {
        var progress = parseInt(data.loaded / data.total * 100, 10);
        $('#progress .progress-bar').css(
            'width',
            progress + '%'
        );
    }).on('fileuploaddone', function (e, data) {
        $.each(data.result.files, function (index, file) {
            var link = $('<a>')
                .attr('target', '_blank')
                .prop('href', file.url);
            $(data.context.children()[index])
                .wrap(link);
        });
    }).on('fileuploadfail', function (e, data) {
        $.each(data.result.files, function (index, file) {
            var error = $('<span/>').text(file.error);
            $(data.context.children()[index])
                .append('<br>')
                .append(error);
        });
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');
});
