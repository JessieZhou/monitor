/**
 * Created by chenlingpeng on 2014/12/28.
 */
monitor = function(uid){
    toastr.options = {
        "closeButton": false,
        "debug": false,
        "positionClass": "toast-bottom-full-width",
        "showDuration": "300",
        "hideDuration": "1000",
        "timeOut": "5000",
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    };



    this.addNewKeyword = function(){
        var keyword = $("#keyword").val();
        $.ajax({
            type: "POST",
            url: "/json/keyword/add",
            data: {keyword: keyword},
            dataType: "json",
            success: function(data){
                if(data && data["status"]==0) {
                    // 成功
                    var ukid = data["ukid"];
                    toastr.success("关键词创建成功");
                } else {
                    toastr.error("关键词创建失败");
                }
            }
        })
    };

    this.deleteKeyword = function(ukid){
        $.ajax({
            type: "POST",
            url: "/json/keyword/del",
            data: {ukid: ukid},
            dataType: "json",
            success: function(data){
                if(data && data["status"]==0) {
                    // 成功
                    toastr.success("关键词删除成功");
                } else {
                    toastr.error("关键词删除失败");
                }
            }
        })
    };

    this.setHistory = function(ukid){
        var start = $("#start");
        var end = $("#end");
        $.ajax({
            type: "POST",
            url: "/json/history/set",
            data: {start: start, end: end, ukid: ukid},
            dataType: "json",
            success: function(data){
                if(data && data["status"]==0) {
                    // 成功
                    toastr.success("历史时间设置成功");
                } else {
                    toastr.error("历史时间设置失败");
                }
            }
        })
    };

    this.addNewAux = function(ukid){
        var aux = $("#aux").val();
        $.ajax({
            type: "POST",
            url: "/json/aux/add",
            data: {aux: aux,ukid: ukid},
            dataType: "json",
            success: function(data){
                if(data && data["status"]==0) {
                    // 成功
                    var ukid = data["ukid"];
                    toastr.success("附属词创建成功");
                } else {
                    toastr.error("附属词创建失败");
                }
            }
        });
    };

    this.deleteAux = function(ukid, auxid){
        var aux = $("#"+auxid).val();
        $.ajax({
            type: "POST",
            url: "/json/aux/del",
            data: {ukid: ukid, aux: aux},
            dataType: "json",
            success: function(data){
                if(data && data["status"]==0) {
                    // 成功
                    toastr.success("附属词删除成功");
                } else {
                    toastr.error("附属词删除失败");
                }
            }
        })
    };


};