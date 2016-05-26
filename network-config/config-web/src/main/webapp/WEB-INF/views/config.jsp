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
<link href="resources/css/group.css" rel="stylesheet">
</head>

<body>
	<div class="navbar">
		<div class="navbar-inner">
			<a class="brand" href="#">配置中心</a>
			<ul class="nav">
				<li><a href="index.html">Home</a></li>
				<li class="active"><a href="config">配置组</a></li>
				<li><a href="group">查看配置</a></li>
				<li><a href="modify">修改配置</a></li>
				<li><a href="user">用户中心</a></li>
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
			<div class="input-append">
				<input class="input-large" id="groupName" type="text"
					placeholder="Type Group Name Here" autofocus>
				<a class="btn" onClick="handle_check()" type="submit"
					rel="tooltip" title="到配置中心查询该配置组是否存在"> <i
					class="icon-check"></i> 确认
				</a>
				<a id="theAddBtn" class="btn btn-success" onClick="handle_add_group()" type="submit"
					rel="tooltip" title="添加该配置组到配置中心"> <i
					class="icon-white icon-plus"></i> 添加
				</a>
			</div>
		</div>
		<div class="alert alert-info width9">
			<button type="button" class="close" data-dismiss="alert">&times;</button>
		<span id="goodMsg">请您先在输入框中输入您想添加的配置组名称，然后点击确认按钮。
		如果服务器确认该配置组不存在，请您点击添加完成配置组的添加。</span>
		</div>
		<div class="main_body">
			<div id="alertMessagePr" class="alert alert-error width9 hide">
				<strong id="alertMessage"></strong>
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
			$("#theAddBtn").attr('disabled', 'true');
		});
	</script>
</body>
</html>