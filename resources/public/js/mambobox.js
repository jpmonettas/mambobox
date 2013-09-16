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

/* For the surpriseme button */
$(function (){
    $(".surprise-me").click(function(){
        window.location="/music-surprise";
    });
});


/* For editing song fields */
$(function (){
    $(".song-edit-button").click(function(){
        $($(this).parent().siblings("input")[0]).prop('disabled',false);
        $(this).hide();
        $($(this).siblings("button")[0]).show();
    });
});

/* For controlling clicks on tags */
$(function (){
    // This is a super horrible patch, but I don't want to be
    // dealing with html and JS now
   $(".label.music-tag.search").click(function(){
       var spanText=$(this).text();
       var regexRes=/(.*) [0-9]+/.exec(spanText);
       var tagName=regexRes?regexRes[1]:spanText;

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

/* For adding a tag */
$(function (){
    $("#main-music-detail-div .tags-box .label-wrapper-div").click(function(){
        var songId=$("#song-id").val();
        var tagHtml=$(this).html();
        var tagName=$(tagHtml).text();
        var tagColor=$(tagHtml).css("background-color");
        var postUrl="/music/" + songId + "/tags/" + tagName;
        if(tagName){
            $.ajax({
                type:"POST",
                url: postUrl,
                success:function(){
                    var outterDiv=$('<div class="btn-group detail-tag">');
                    var tagSpan=$('<span class="label music-tag dropdown-toggle" data-toggle="dropdown">')
                        .css("background-color",tagColor)
                        .text(tagName);
                    var tagMenu=$('<ul class="dropdown-menu"><li>Quitar</li></ul>');
                    outterDiv.append(tagSpan);
                    outterDiv.append(tagMenu);
                    $("#main-music-detail-div .tags").append(outterDiv);
                    $(".detail-tag li").click(removeTagFromSong);
                }
            });
        }
    });
});

/* For removing a tag */
var removeTagFromSong=function(){
       var tagElement=$(this).parents(".detail-tag");
       var tagName=$($(this).parent().siblings("span")[0]).text();
       var songId=$("#song-id").val();
       var deleteUrl='/music/' + songId + '/tags/' + tagName;
       $.ajax({
                type:"DELETE",
                url: deleteUrl,
            }).success(function(){
                tagElement.remove(); 
            });
}

$(function (){
   $(".detail-tag li").click(removeTagFromSong);
});

/* For adding a song to favourites */
$(function (){
   $("#add-to-favourites").click(function(){
       var songId=$("#song-id").val();
       var postUrl="/current-user/favourites/" + songId;
       var addButton=$(this);
       if(songId){
           $.ajax({
                type:"POST",
               url: postUrl,
               success:function(){
                   addButton.remove();
               }
           });
       }
       
   });
});

/* For changing the search base collection */
$(function (){
   $("input[name='collection-filter']").change(function(){
      $("#search-form").submit();
   });
});

/* For removing a favourite*/
$(function (){
   $(".remove-fav-btn").click(function(){
       var resultLi=$(this).parents("li.result");
       var songId=$(this).siblings("input[name=song-id]").val();
       var deleteUrl='/current-user/favourites/' + songId;
       $.ajax({
                type:"DELETE",
                url: deleteUrl,
            }).success(function(){
                resultLi.remove();
            });
    });
});

/* For removing a video link */
$(function (){
   $(".delete-video-button").click(function(){
       var videoWraperDiv=$(this).parents(".video-wrapper");
       var link=videoWraperDiv.find("input").val();
       var songId=$("#song-id").val();
       var deleteUrl='/music/' + songId + '/links/' + encodeURIComponent(link);
       $.ajax({
                type:"DELETE",
                url: deleteUrl,
            }).success(function(){
                videoWraperDiv.remove(); 
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
    $("#jquery_jplayer_1").jPlayer({
        ready: function () {
            $(this).jPlayer("setMedia", {
                mp3: $("#song-file").val()
            });
        },
        swfPath: "/swf",
        supplied: "mp3"
    });

});

// For the upload plugin
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
                .text('Abortar')
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
        filesContainer: $('#upload_files_container'),
        dataType: 'json',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(mp3)$/i,
        maxFileSize: 20000000, // 20 MB
    }).on('fileuploadadd', function (e, data) {
        data.context = $('<div class="col-md-4 col-md-offset-4"/>').appendTo('#files');
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
                //.append('<br>')
                //.append(file.error);
                .append($('<div class="alert alert-danger"/>').text("Solo archivos mp3 de 20MB soportados actualmente"));
        }
        if (index + 1 === data.files.length) {
            data.context.find('button')
                .text('Subir')
                .prop('disabled', !!data.files.error);
        }
    }).on('fileuploadprogress', function (e, data) {
        var progress = parseInt(data.loaded / data.total * 100, 10);
        $('#progress .progress-bar').css(
            'width',
            progress + '%'
        );
    }).on('fileuploaddone', function (e, data) {
        $.each(data.result.files, function (index, file) {
            if(!file.error){
                var link = $('<a>')
                    .attr('target', '_blank')
                    .prop('href', file.url);
                $(data.context.children()[index])
                    .wrap(link);
            }else{
                var error = $('<div class="alert alert-danger"/>').text(file.error);
                $(data.context.children()[index])
                    .append('<br>')
                    .append(error);
            }  
        });
              
    }).on('fileuploadfail', function (e, data) {
        $.each(data.result.files, function (index, file) {
            var error = $('<div class="alert alert-danger"/>').text(file.error);
            $(data.context.children()[index])
                .append('<br>')
                .append(error);
        });
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');
});

