<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>配置中心</title>
<link rel="icon" href="./resources/img/icon.ico" type="image/ico">
<!-- Le styles -->
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">
<link href="resources/css/user.css" rel="stylesheet">
</head>

<body>
	<div class="navbar">
		<div class="navbar-inner">
			<a class="brand" href="#">配置中心</a>
			<ul class="nav">
				<li><a href="index.html">Home</a></li>
				<li><a href="config">配置组</a></li>
				<li><a href="group">查看配置</a></li>
				<li><a href="modify">修改配置</a></li>
				<li class="active"><a href="user">用户中心</a></li>
			</ul>
			<span class="nav user-info"> <i class="icon-hand-right"></i>&nbsp;${userName}
			</span>
			<ul class="nav pull-right">
				<li><a href="#">Link</a></li>
				<li class="divider-vertical"></li>
				<li class="dropdown"><a href="#" class="dropdown-toggle"
					data-toggle="dropdown">系统<b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a data-toggle="modal" data-target="#mymodal"
							href="#mymodal">Sign In</a></li>
						<li><a href="login?signout=3">Sign Out</a></li>
						<li class="divider"></li>
						<li><a href="#">About This</a></li>
					</ul></li>
			</ul>
		</div>
		<!-- /navbar-inner -->
	</div>
	<!-- /navbar -->
	<div class="main">
		<!--modal's content-->
		<div id="mymodal" class="modal hide fade">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h3 id="results">登录到用户中心</h3>
			</div>
			<!--Modal header-->
			<div class="modal-body">
				<div class="row">
					<div class="span4">
						<form id="login" action="login" method="post"
							class="modal-form form-horizontal">
							<div class="control-group">
								<label class="control-label">User Name:</label>
								<div class="controls">
									<input type="text" id="userName" name="username" required>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label">Password:</label>
								<div class="controls">
									<input type="password" id="password" name="password" required>
								</div>
							</div>
						</form>
					</div>
					<div class="span1" id="target"></div>
				</div>
			</div>
			<!--Modal body-->
			<div class="modal-footer">
				<input type="submit" class="btn btn-primary" value="Sign In"
					form="login"> <input type="button" class="btn btn-danger"
					data-dismiss="modal" value="Close" form="login">
			</div>
			<!--Modal footer-->
		</div>
		<!--Modal-->
		<div class="main_query">
			<div class="row">
				<div class="span4 bs-docs-useradd">
					<form id="addUserForm" class="form-horizontal">
						<div class="control-group" style="margin-bottom: 5px;">
							<label class="control-label" style="width: 60px;"
								for="addUserName">用户名</label>
							<div class="controls" style="margin-left: 80px;">
								<input type="text" id="addUserName" name="username"
									placeholder="Type User Name Here">
							</div>
						</div>
						<div class="control-group" style="margin-bottom: 5px;">
							<label class="control-label" style="width: 60px;"
								for="addPassword">密码</label>
							<div class="controls" style="margin-left: 80px;">
								<input type="password" id="addPassword" name="password"
									placeholder="Type Password Here">
							</div>
						</div>
						<div class="control-group" style="margin-bottom: 15px;">
							<label class="control-label" style="width: 60px;" for="addUserRole">角色</label>
							<div class="controls" style="margin-left: 80px;">
								<div class="input-append">
								<div class="btn-group">
									<input class="input-medium" id="addUserRole" type="text"
										 name="userrole" placeholder="Type User Role Here">
									<button class="btn dropdown-toggle" data-toggle="dropdown">
										<span class="caret"></span>
									</button>
									<ul class="dropdown-menu">
										<li><a href="#" onclick="add_user_role('ADMIN')">ADMIN</a></li>
										<li><a href="#" onclick="add_user_role('OP')">OP</a></li>
										<li><a href="#" onclick="add_user_role('USER')">USER</a></li>
										<li><a href="#" onclick="add_user_role('NODE')">NODE</a></li>
									</ul>
								</div>
								</div>
							</div>
						</div>
						<div id="alertMessagePr" class="alert alert-error hide">
						    <button type="button" class="close" onclick="user_alert_hide()">&times;</button>
							<strong id="alertMessage"></strong>
						</div>
						<div class="control-group" style="margin-bottom: 0px;">
							<div class="controls" style="margin-left: 80px;">
								<a id="theAddBtn" class="btn btn-success" onclick="handle_add_user()" type="submit"
									rel="tooltip" title="添加该用户到配置中心"> <i
									class="icon-white icon-plus"></i> 添加
								</a>
							</div>
						</div>
					</form>
				</div>
				
				<div class="span4 bs-docs-usermg">
					<div class="input-append">
						<input class="input-large" id="theUserName" type="text"
							name="theUserName" placeholder="Type User Name Here"> <a class="btn btn-info"
							onClick="handle_user_query()" type="submit" rel="tooltip"
							title="到配置中心查询该用户"> <i class="icon-white icon-search"></i> 查询
						</a>
					</div>
					<span id="queryUserMsg" class="help-block">先输入用户名进行查询，如果不存在，则请使用添加
						新用户功能进行添加，否则则可以修改用户的密码和角色。</span>
					<div class="input-append">
						<input class="input-me" id="theUserPass" type="text"
							name="theUserPass" placeholder="Type User Password Here">
						<a class="btn btn-success" onClick="handle_user_passwd()" id="modifyUserPassword"
							type="submit" rel="tooltip" title="修改该用户的密码"> <i
							class="icon-white icon-fire"></i> 修改密码
						</a>
					</div>
					<div class="input-append">
						<div class="btn-group">
							<input class="input-medium" id="theUserRole" type="text"
								placeholder="Type User Role Here">
							<button class="btn dropdown-toggle" data-toggle="dropdown">
								<span class="caret"></span>
							</button>
							<ul class="dropdown-menu">
								<li><a href="#" onclick="mod_user_role('ADMIN')">ADMIN</a></li>
								<li><a href="#" onclick="mod_user_role('OP')">OP</a></li>
								<li><a href="#" onclick="mod_user_role('USER')">USER</a></li>
								<li><a href="#" onclick="mod_user_role('NODE')">NODE</a></li>
							</ul>
						</div>
						<a class="btn btn-success" onClick="handle_user_role()" id="modifyUserRole"
							type="submit" rel="tooltip" title="修改该用户的角色"> <i
							class="icon-white icon-retweet"></i> 修改角色
						</a>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="span4 bs-docs-auth">
					<form class="form-horizontal">
						<div class="control-group">
							<label class="control-label" style="width: 60px;"
								for="authUserName">用户名</label>
							<div class="controls" style="margin-left: 80px;">
								<input type="text" id="authUserName"
									placeholder="Type User Name Here">
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" style="width: 60px;"
								for="authGroupName">配置组</label>
							<div class="controls" style="margin-left: 80px;">
								<input type="text" id="authGroupName"
									placeholder="Type Group Name Here">
							</div>
						</div>
						<div id="authMessagePr" class="alert alert-error hide">
						    <button type="button" class="close" onclick="auth_alert_hide()">&times;</button>
							<strong id="authMessage"></strong>
						</div>
						<div class="control-group">
							<div class="controls" style="margin-left: 80px;">
								<button class="btn btn-success" onClick="handle_auth_add()">添加授权</button>
								<button class="btn btn-danger" onClick="handle_auth_remove()">取消授权</button>
							</div>
						</div>
					</form>
				</div>
			</div>
			
			<div class="alert alert-info">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
				<span id="goodMsg">欢迎访问用户中心！本页面具备管理用户和授权相关的所有功能。</span>
			</div>
		</div>
	</div>
	<!-- /container -->

	<!-- Placed at the end of the document so the pages load faster -->
	<script src="bootstrap/js/jquery.min.js"></script>
	<script src="bootstrap/js/bootstrap.min.js"></script>
	<script src="resources/js/index.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			init_bind();
			user_modify_disable();
		});
	</script>
</body>
</html>