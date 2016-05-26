Date.prototype.format = function(format) {
        var o = {
        "M+" : this.getMonth()+1, // month
        "d+" : this.getDate(),    // day
        "h+" : this.getHours(),   // hour
        "m+" : this.getMinutes(), // minute
        "s+" : this.getSeconds(), // second
        "q+" : Math.floor((this.getMonth()+3)/3),  // quarter
        "S" : this.getMilliseconds() // millisecond
        };
        if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
        (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)if(new RegExp("("+ k +")").test(format))
        format = format.replace(RegExp.$1,
        RegExp.$1.length==1 ? o[k] :
        ("00"+ o[k]).substr((""+ o[k]).length));
        return format;
};

var mainBoxy = new Array();

function init_bind() {
	$("#login").submit(function() {
		var str = $(this).serialize();
		$.ajax({
			type : "POST",
			url : "login",
			data : str
		}).done(function(data) {
			if (data.msg != null) {
				if (data.msg == "SUCCESS") {
					window.location = "group";
				} else {
					$("#results").text(data.msg);
				}
			} else {
				$("#results").text("服务器返回未知错误");
			}
			res_annimation();
		}).fail(function() {
			$("#results").text("网络连接错误");
			res_annimation();
		});
		return false;
	});
}

function res_annimation() {
	$("#results").addClass("text-error");
	$("#results").animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000);
}

function handle_query() {
	$.ajax({
		url : "group/" + encodeURIComponent($("#groupName").attr("value"))
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "SUCCESS") {
				$("#alertMessage").parent().addClass("hide");
				create_table(data.list);
				return;
			} else {
				$("#alertMessage").text(data.msg);
			}
		} else {
			$("#alertMessage").text("服务器返回未知错误");
		}
		$("#alertMessage").parent().removeClass("hide");
		$("#dataTable").addClass("hide");
	}).fail(function() {
		$("#alertMessage").text("网络连接错误");
		$("#alertMessage").parent().removeClass("hide");
		$("#dataTable").addClass("hide");
	});
}

function htmlEncode(str) {
    var s = "";
    if (str.length == 0) return "";
    s = str.replace(/&/g, "&amp;");
    s = s.replace(/</g, "&lt;");
    s = s.replace(/>/g, "&gt;");
    s = s.replace(/\'/g, "&#39;");
    s = s.replace(/\"/g, "&quot;");
    return s;  
}

function create_table(list) {
	var obj = document.getElementById("dataTable");
	for ( var i = obj.rows.length - 1; i > 0; --i) {
		obj.deleteRow(i);
	}
	var len = list.length;
	for (var k = 0, i = 1; k < len; ++k, ++i) {
		var newTr = obj.insertRow(i); // 添加一行
		newTr.id = "dataTableRow" + i;
		var jsonItem = list[k];
		var newTd = newTr.insertCell(0); // 添加一列
        newTd.innerHTML = jsonItem.key;
        var newTd = newTr.insertCell(1); // 添加一列
        var myDate=new Date(jsonItem.updateTime);
        newTd.innerHTML = myDate.format('yyyy-MM-dd hh:mm:ss');
        var newTd = newTr.insertCell(2); // 添加一列
        var s = jsonItem.value;
        if (s.length > 60) {
        	s = s.slice(0, 60);
        	s = htmlEncode(s);
        	mainBoxy[k] = jsonItem;
        	s += "... <a class='pull-right' href='#' onclick='make_popup(" +
        			k + ")'><i class='icon-qrcode'></i>详细&raquo;</a>";
        } else {
        	s = htmlEncode(s);
        }
        newTd.innerHTML = s;
	}
	for (var k = 2; k < len + 1; k += 2) {
		$("#dataTableRow" + k).addClass("table_row");
	}
	$("#dataTable").removeClass("hide");
}

function make_popup(k) {
	//alert("Go " + mainBoxy[k].key);
	$("#detailKey").text(mainBoxy[k].key);
	$("#detailValue").text(mainBoxy[k].value);
	var s = "groupId=" + mainBoxy[k].groupId + "&key=" + encodeURIComponent(mainBoxy[k].key);
	$("#detailModify").attr("href", "modify?" + s);
	$("#modalDetail").modal();
}

function check_alert() {
	if ($("#alertMessage").html() != "") {
		$("#alertMessage").parent().removeClass("hide");
	}
}

//=================================================================
// For modify Page
//=================================================================

function handle_modify() {
	$.ajax({
		url : "modify/" + encodeURIComponent($("#groupName").attr("value"))
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "SUCCESS") {
				create_list(data.list);
				return;
			} else {
				$("#alertMessage").text(data.msg);
			}
		} else {
			$("#alertMessage").text("服务器返回未知错误");
		}
		handle_modify_disable();
	}).fail(function() {
		$("#alertMessage").text("网络连接错误");
		handle_modify_disable();
	});
}

function create_list(list) {
	var len = list.length;
	var str = "";
	var gName = encodeURIComponent($("#groupName").attr("value"));
	for (var i = 0; i < len; ++i) {
		var s = "modify?groupName=" + gName + "&key=" + encodeURIComponent(list[i]);
		str += '<li><a href="' + s + '">' + list[i] + '</a></li>';
	}
	$("#selectKey").html(str);
	handle_modify_enable();
	create_list_annimation();
}

function handle_modify_enable() {
	$("#typedKey").removeAttr('disabled');
	$("#selectKeyBtn").removeAttr('disabled');
}

function create_list_annimation() {
	$("#alertMessagePr").clearQueue();
	$("#alertMessagePr").removeClass("hide");
	$("#alertMessagePr").removeClass("alert-error");
	$("#alertMessagePr").addClass("alert-success");
	$("#alertMessage").text("查询成功，请进行下一步");
	$("#alertMessagePr").animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000, function() {
	    // Animation complete.
		$("#alertMessagePr").addClass("hide");
		$("#alertMessagePr").addClass("alert-error");
		$("#alertMessagePr").removeClass("alert-success");
	  }
	);
}

function handle_modify_disable() {
	$("#typedKey").attr('disabled', 'true');
	$("#selectKeyBtn").attr('disabled', 'true');
	$("#alertMessagePr").stop();
	$("#alertMessagePr").removeClass("hide");
}

function check_data() {
	if ($("#alertMessage").html() != "") {
		$("#alertMessage").parent().removeClass("hide");
	}
	if ($("#selectItemValue").val() != "") {
		$("#theSaveBtn").removeAttr('disabled');
		$("#selectItemValue").removeClass("hide");
		$("#goodMsg").text("您可以直接在下框中修改配置，然后点击<Save changes>按钮进行保存。");
		$("#selectItemValue").focus();
	}
	$('#typedKey').keyup(function () {
		if ($('#typedKey').val() != "") {
			$('#theAddBtn').removeAttr('disabled');
		} else {
			$('#theAddBtn').attr('disabled', 'true');
		}
	});
}

function add_value_area() {
	if ($('#typedKey').val() != "") {
		$("#typedKey").attr('disabled', 'true');
		$("#theSaveBtn").removeAttr('disabled');
		$("#selectItemValue").removeClass("hide");
		$("#goodMsg").text("您可以直接在下框中修改配置，然后点击<Save changes>按钮进行保存。");
		$("#selectItemValue").focus();
	}
}

function handle_update() {
	var s = $('#saveForm').serialize();
	s += '&itemKey=' + encodeURIComponent($("#typedKey").val());
	$.ajax({
		type : "POST",
		url : "modify",
		data : s
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "SUCCESS") {
				$("#goodMsg").text("配置修改成功，请切换到<查看配置>栏目进行确认。");
				$("#alertMessagePr").addClass("hide");
				return;
			} else {
				$("#alertMessage").text(data.msg);
			}
		} else {
			$("#alertMessage").text("服务器返回未知错误");
		}
		$("#alertMessagePr").removeClass("hide");
	}).fail(function() {
		$("#alertMessage").text("网络连接错误");
		$("#alertMessagePr").removeClass("hide");
	});
}


//=================================================================
//For config Page
//=================================================================

function handle_check() {
	$.ajax({
		url : "config?groupName=" + encodeURIComponent($("#groupName").attr("value"))
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "SUCCESS") {
				handle_config_enable();
				return;
			} else {
				$("#alertMessage").text(data.msg);
			}
		} else {
			$("#alertMessage").text("服务器返回未知错误");
		}
		handle_config_disable();
	}).fail(function() {
		$("#alertMessage").text("网络连接错误");
		handle_config_disable();
	});
}

function handle_config_enable() {
	$("#alertMessage").text("该配置组已经被确认，可以添加");
	$("#theAddBtn").removeAttr('disabled');
	$("#groupName").attr('disabled', 'true');
	$("#alertMessagePr").removeClass('hide');
}

function handle_config_disable() {
	$("#theAddBtn").attr('disabled', 'true');
	$("#alertMessagePr").removeClass('hide');
}

function handle_add_group() {
	if ($("#theAddBtn").attr('disabled')) {
		return;
	}
	$.ajax({
		type : "POST",
		url : "config",
		data : "groupName=" + encodeURIComponent($("#groupName").attr("value"))
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "SUCCESS") {
				$("#alertMessage").text("配置组添加成功，请切换到<查看配置>栏目进行确认。");
			} else {
				$("#alertMessage").text(data.msg);
			}
		} else {
			$("#alertMessage").text("服务器返回未知错误");
		}
		handle_add_disable();
	}).fail(function() {
		$("#alertMessage").text("网络连接错误");
		handle_add_disable();
	});
}

function handle_add_disable() {
	$("#theAddBtn").attr('disabled', 'true');
	$("#groupName").removeAttr('disabled');
}



//=================================================================
//For user Page
//=================================================================

function add_user_role(role) {
	$("#addUserRole").val(role);
}

function handle_add_user() {
	var s = $('#addUserForm').serialize();
	$.ajax({
		type : "POST",
		url : "user/add",
		data : s
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "Add User Success.") {
				user_alert_annimation("用户添加成功，请继续。");
				return;
			} else if (data.msg == "User already exist.") {
				$("#alertMessage").text("用户已经存在，请使用‘用户管理’模块进行维护。");
			} else {
				$("#alertMessage").text(data.msg);
			}
		} else {
			$("#alertMessage").text("服务器返回未知错误");
		}
		$("#alertMessagePr").removeClass("hide");
	}).fail(function() {
		$("#alertMessage").text("网络连接错误");
		$("#alertMessagePr").removeClass("hide");
	});
}

function user_alert_hide() {
	$("#alertMessagePr").addClass("hide");
}

function user_alert_annimation(msg) {
	$("#alertMessagePr").clearQueue();
	$("#alertMessagePr").removeClass("hide");
	$("#alertMessagePr").removeClass("alert-error");
	$("#alertMessagePr").addClass("alert-success");
	$("#alertMessage").text(msg);
	$("#alertMessagePr").animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000, function() {
	    // Animation complete.
		$("#alertMessagePr").addClass("hide");
		$("#alertMessagePr").addClass("alert-error");
		$("#alertMessagePr").removeClass("alert-success");
	  }
	);
}

function mod_user_role(role) {
	$("#theUserRole").val(role);
}

function handle_user_query() {
	$.ajax({
		url : "user/" + encodeURIComponent($("#theUserName").attr("value"))
	}).done(function(data) {
		if (data.msg != null) {
			if (data.msg == "SUCCESS") {
				user_modify_enable();
				$("#theUserRole").val(data.role);
				$("#queryUserMsg").text("用户查询成功，请继续修改密码或角色。");
				return;
			} else {
				$("#queryUserMsg").text(data.msg);
			}
		} else {
			$("#queryUserMsg").text("服务器返回未知错误。");
		}
		user_modify_disable();
	}).fail(function() {
		$("#queryUserMsg").text("网络连接错误。");
		user_modify_disable();
	});
}

function user_modify_disable() {
	$("#modifyUserPassword").attr('disabled', 'true');
	$("#modifyUserRole").attr('disabled', 'true');
}

function user_modify_enable() {
	$("#modifyUserPassword").removeAttr('disabled');
	$("#modifyUserRole").removeAttr('disabled');
}

function handle_user_passwd() {
	$("#queryUserMsg").text("还没有实现1。");
}

function handle_user_role() {
	$("#queryUserMsg").text("还没有实现2。");
}

function handle_auth_add() {
	auth_alert_annimation("还没有实现3。");
}

function handle_auth_remove() {
	auth_alert_annimation("还没有实现4。");
}

function auth_alert_hide() {
	$("#authMessagePr").addClass("hide");
}

function auth_alert_annimation(msg) {
	$("#authMessagePr").clearQueue();
	$("#authMessagePr").removeClass("hide");
	$("#authMessagePr").removeClass("alert-error");
	$("#authMessagePr").addClass("alert-success");
	$("#authMessage").text(msg);
	$("#authMessagePr").animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000).animate({
		opacity : 0.2
	}, 700).animate({
		opacity : 1
	}, 1000, function() {
	    // Animation complete.
		$("#authMessagePr").addClass("hide");
		$("#authMessagePr").addClass("alert-error");
		$("#authMessagePr").removeClass("alert-success");
	  }
	);
}
