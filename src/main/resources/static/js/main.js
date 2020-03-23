/**
    *
    * Created by yl
    * 2018/9/14.
    */
var layer= layui.layer;
var form = layui.form;
var table = layui.table;
var laydate = layui.laydate;
/**
 * layer
 * icon 0感叹号 1正确 2错误 3问号 4锁定 5哭脸 6笑脸
 * anim 0平滑放大-默认 1从上掉落 2从最底部往上滑入 3从左滑入 4从左翻滚 5渐显 6抖动
 * @param msg
 */
function msgSuccess(msg,fun){
    layer.msg(msg,{icon:1},fun)
}
function msgError(msg,fun){
    layer.msg(msg,{icon:2,anim:6},fun)
}
function msgInfo(msg,fun){
    layer.msg(msg,{icon:0,anim:4},fun)
}

/**
 * 发送异步请求，加入load和结果过滤
 * @param params
 */
function ajax(params) {
    var load=layer.load();
    $.ajax({
        url:params.url,
        type:params.type,
        data:params.data,
        dataType:params.dataType,
        success:function (data) {
            layer.close(load);
            if(resultFilter(data))
                params.success(data);
        },
        error:function () {
            console.log("request error - url:"+params.url);
            layer.close(load);
            msgError("请求失败");
        }
    });
}
function ajaxGet(url,success){
    ajax({
        url:url,
        type:"get",
        dataType:"json",
        success:success
    });
}
function ajaxPost(url,data,success){
    ajax({
        url:url,
        type:"post",
        data:data,
        dataType:"json",
        success:success
    });
}
function ajaxFile(url,data,success) {
    var load=layer.load();
    $.ajax({
        url:url,
        type:"post",
        data:data,
        dataType:"json",
        async: true,
        cache: false,
        contentType: false,
        processData: false,
        success:function (data) {
            layer.close(load);
            if(resultFilter(data))
                success(data);
        },
        error:function () {
            console.log("request error - url:"+url);
            layer.close(load);
            msgError("请求失败");
        }
    });
}

/**
 * 请求静态资源
 * @param params
 */
function ajaxStatic(params) {
    var load=layer.load();
    $.ajax({
        url:params.url,
        type:"get",
        success:function (data) {
            layer.close(load);
            params.success(data);
        },
        error:function () {
            console.log("request error - url:"+params.url);
            layer.close(load);
            layer.msg("请求失败",{icon:2});
        }
    });
}
/**
 * 地址跳转
 * @param url
 */
function jump(url){
    window.location=url;
}
/**
 * 非空判断
 * @param obj
 * @returns true:空
 */
function isNull(obj){
    return obj==undefined || obj==null || obj=='';
}
/**
 * 返回信息过滤
 * @param data
 * @returns {boolean}
 */
function resultFilter(data){
    if(isNull(data)){
        msgError("未知错误");
        return false;
    }
    if(data.code==0){
        return true;
    }if(data.code==1){
        login();
    }else if(!isNull(data.code)){
        if("未登录"==data.msg){
            login();
        }else{
            msgError(isNull(data.msg)?"空":data.msg);
        }
    }else{
        msgError("未知错误");
    }
    return false;
}
/**
 * 获取url地址中的参数
 * @param name
 * @returns {null}
 */
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); // 构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  // 匹配目标参数
    if (r != null) return unescape(r[2]); return null; // 返回参数值
}
/**
 * 字符串转时间戳 (s)
 * @param str 2017 2017-09 2017-09-01 2017-09-01 09:01:01
 * @returns {string} 1538015573
 */
function strToTime(str) {
    if(!isNull(str)){
        str=str.replace(/-/g,'/');
        var len=str.length;
        switch (len){
            case 4:
                str+="/01/01 00:00:00";break;
            case 7:
                str+="/01 00:00:00";break;
            case 10:
                str+=" 00:00:00";break;
            case 13:
                str+=":00:00";break;
            case 16:
                str+=":00";break;
        }
        if(str.length==19){
            return str
            //return new Date(str).getTime()/1000;
        }
    }
    return "1970/01/01 08:00:00";
    //return new Date("1970/01/01 08:00:00").getTime()/1000;//东八区
}
/**
 * 时间戳转时间字符串 (s)
 * @param timestamp 1538015573
 * @returns {string} 2017-09-01 09:01:01
 */
function timeToStr(timestamp) {
    var d = new Date(timestamp * 1000);
    var date = (d.getFullYear()) + "-" +
        ((d.getMonth() + 1)<10?'0'+(d.getMonth() + 1):(d.getMonth() + 1)) + "-" +
        (d.getDate()<10?'0'+d.getDate():d.getDate()) + " " +
        (d.getHours()<10?'0'+d.getHours():d.getHours()) + ":" +
        (d.getMinutes()<10?'0'+d.getMinutes():d.getMinutes()) + ":" +
        (d.getSeconds()<10?'0'+d.getSeconds():d.getSeconds());
    return date;
}
/**
 * 字符串到天
 * @param str 2017-09-01 09:01:01
 * @returns {string} 2017-09-01
 */
function strToDay(str){
    return (!isNull(str)&& str.length>=10)?str.substring(0,10):"";
}
function nowTime(){
    var time=new Date().getTime();
    return timeToStr(time/1000);
}

/**
 * 获取文件后缀
 * @param str file.pdf
 * @returns {string} pdf
 */
function getFileSuffix(str){
    if(isNull(str)){
        return "";
    }
    return str.substring(str.lastIndexOf(".")+1);
}


/**
 * 验证码点击更换
 */
function changeCheckCode() {
    $(".checkCode>img").attr("src","checkCode?time="+new Date().getTime());
}


$(function () {
    /**
     * 限制带有number属性的输入框只能输入数字
     */
   $("input[number]").keydown(function(evt) {//按下键
       var iKeyCode = window.event?evt.keyCode:evt.which;
       /*
        ascii码说明：
        8：退格键
        46：delete
        37-40： 方向键
        48-57：小键盘区的数字
        96-105：主键盘区的数字
        110,190：小键盘区和主键盘区的小数点
        189,109：小键盘区和主键盘区的负号
        13：回车
        9： Tab
        */
       if(((iKeyCode>=48) && (iKeyCode<=57)) || ((iKeyCode>=96) && (iKeyCode<=105)) || ((iKeyCode>=37) && (iKeyCode<=40)) ||iKeyCode===8|| iKeyCode==46 || iKeyCode==109|| iKeyCode==110|| iKeyCode==189||iKeyCode==190) {
       } else {
           if (window.event) {
               event.returnValue = false;//IE
           } else {
               evt.preventDefault();//Firefox
           }
       }
   }).keyup(function() {//松开键
       $(this).val($(this).val().replace(/[^0-9-\.]/g,''));
   }).blur(function () {
       if(!isNull($(this).val()) && !$(this).val().match(/^[-+]?[0-9]*\.?[0-9]+$/g)){
           msgError("只能输入数字");
           $(this).val("");
           $(this).focus();
       }
   });

   //登录验证
    /*ajaxGet("/getUser",function (data) {
    })*/

    //表单监听
    layui.use('form', function(){
        var form = layui.form;
        //监听提交
        form.on('submit(login)', function(data){
            ajaxPost("login",data.field,function () {
                layer.msg("登录成功",{icon:1,time:1000},function(){
                    window.location.reload();
                });
            });
            return false;
        });
    });

});


function login(){
    layer.closeAll();
    var content='<div class="login" style="margin-top: 0;min-height: 320px">'
                +'<form method="post" class="layui-form" >'
                    +'<input name="username" value="test1" placeholder="用户名"  type="text" lay-verify="required" class="layui-input" ><hr class="hr15">'
                    +'<input name="password" value="123456" lay-verify="required" placeholder="密码"  type="password" class="layui-input"><hr class="hr15">'
                    +'<div class="layui-form-item">'
                        +'<div class="layui-input-inline">'
                            +'<input name="checkCode" value="" lay-verify="required" placeholder="验证码"  type="text" class="layui-input">'
                        +'</div>'
                        +'<div class="checkCode"><img onclick="changeCheckCode()" src="/checkCode"/></div>'
                    +'</div>'
                    +'<input value="登录" lay-submit lay-filter="login" style="width:100%;" type="submit">'
                    +'<hr class="hr20" >'
                    +'<a onclick="forgetPwd()">忘记密码</a>'
                +'</form>'
            +'</div>';
    layer.open({
        type: 1,
        title:"登录框",
        skin: 'layui-layer-rim', //加上边框
        area: ['440px', '460px'], //宽高
        content: content,
        cancel:function () {
            login();//禁止关闭登录窗口
            //window.location="index.html";
        }
    });
}
function loginSubmit(){
    var load=layer.load();
    $.ajax({
        url:"/login",
        type:"post",
        dataType:"JSON",
        data:{username:$("#loginUsername").val(),password:$("#loginPassword").val()/*,checkCode:$("#loginCheckCode").val()*/},
        success:function(d){
            layer.close(load);
            if(resultFilter(d)){
                layer.closeAll();
                layer.msg("登录成功",{icon:1,time:1000},function(){
                    window.location.reload();
                });
            }
        }
    });
}
function regist(){
    layer.closeAll();
    var content='<div class="col-xs-12">'+
        '<input class="form-control" style="margin-top:20px" type="text" id="registUsername" placeholder="请输入账号" />'+
        '<input class="form-control" style="margin-top:20px" type="password" id="registPassword" placeholder="请输入密码" />'+
        '<input class="form-control" style="margin-top:20px" type="password" id="registRePassword" placeholder="重复密码" />'+
        '<input class="form-control" style="margin-top:20px" type="text" id="registName" placeholder="请输入姓名" />'+
        '<input class="form-control" style="margin-top:20px" type="text" id="registPhone" placeholder="请输入电话" />'+
        '<input class="form-control" style="margin-top:20px;" type="text" id="registCheckCode" placeholder="验证码" />'+
        '<div class="col-xs-12" style="margin-top:4px"><img onclick="changeCheckCode()" class="checkCode" src="/checkCode"/></div>'+
        '<div class="col-xs-4 btn btn-info" style="margin-top:20px" onclick="login()">登录</div>'+
        '<div class="col-xs-8 btn btn-success" style="margin-top:20px" onclick="registSubmit()">注册</div>'+
        ''+
        '</div>';
    layer.open({
        type: 1,
        title:"注册框",
        skin: 'layui-layer-rim', //加上边框
        area: ['50%', '80%'], //宽高
        content: content,
        cancel:function () {
            login();
        }
    });
}
function registSubmit(){
    if($("#registPassword").val()!=$("#registRePassword").val()){
        layer.msg("两次密码不一致",{icon:2,time:1000});
        $("#registRePassword").val("");
        return;
    }
    var load=layer.load();
    $.ajax({
        url:"/user",
        type:"post",
        dataType:"JSON",
        data:{username:$("#registUsername").val(),
            name:$("#registName").val(),
            phone:$("#registPhone").val(),
            password:$("#registPassword").val(),
            checkCode:$("#registCheckCode").val()},
        success:function(d){
            layer.close(load);
            if(resultFilter(d)){
                layer.closeAll();
                layer.msg("注册成功",{icon:1,time:1000},function(){
                    login();
                });
            }
        }
    });
}

let forgetPwdIndex;
function forgetPwd(){
    let html="<div style='width: 300px;margin: 10px auto;'>"
        +"<input class='layui-input' id='fPwdUsername' placeholder='请输入您的用户名' type='text'><br>"
        +"<button class='layui-btn' onclick='forgetPwdSubmit()'>提交</button>"
        +"</div>";
    forgetPwdIndex=layer.open({
        type: 1,
        skin: 'layui-layer-rim', //加上边框
        area: ['420px', '300px'], //宽高
        title:"找回密码",
        content: html
    });
}
function forgetPwdSubmit(){
    let fPwdUsername=$("#fPwdUsername").val();
    if(isNull(fPwdUsername)){
        msgInfo("用户名不能为空");
    }else{
        ajaxPost("./forgetPwd",{username:fPwdUsername},function () {
            msgSuccess("找回成功，请到您的预留邮箱中查看密码");
            layer.close(forgetPwdIndex);
        })
    }
}

