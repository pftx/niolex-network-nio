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
</head>

<body>
	<div class="navbar">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="#">配置中心</a>
				<div class="nav-collapse">
					<ul class="nav">
						<li class="active"><a href="index.html">Home</a></li>
						<li><a href="config">配置组</a></li>
						<li><a href="group">查看配置</a></li>
						<li><a href="modify">修改配置</a></li>
						<li><a href="user">用户中心</a></li>
					</ul>
					<ul class="nav pull-right">
						<li><a href="#">Link</a></li>
						<li class="divider-vertical"></li>
						<li class="dropdown"><a href="#" class="dropdown-toggle"
							data-toggle="dropdown">系统<b class="caret"></b></a>
							<ul class="dropdown-menu">
								<li><a data-toggle="modal" data-target="#mymodal"
									href="#mymodal">Sign In</a></li>
								<li><a href="login?signout=2">Sign Out</a></li>
								<li class="divider"></li>
								<li><a href="#">About This</a></li>
							</ul></li>
					</ul>
				</div>
				<!-- /.nav-collapse -->
			</div>
		</div>
		<!-- /navbar-inner -->
	</div>
	<!-- /navbar -->
	<div class="container">
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
		<div class="alert alert-success hide">
			<button type="button" class="close" data-dismiss="alert">&times;</button>
			<strong id="alertMessage">${alertMessage}</strong>
		</div>
		<!-- Main hero unit for a primary marketing message or call to action -->
		<div class="hero-unit">
			<h1 id="mainMessage">欢迎来到配置中心!</h1>
			<p>配置中心管理所有的动态配置，以配置组为基本管理单位，每个配置由一对KV组成.</p>
			<p>为了安全起见，配置中心需要登录.</p>
			<p>
				<a class="btn btn-primary btn-large" data-toggle="modal"
					data-target="#mymodal" href="#mymodal">Sign In &raquo;</a>
			</p>
		</div>
		<!-- Example row of columns -->
		<div class="row">
			<div class="span6">
				<h2>数据模型</h2>
				<p>
					<img alt="原理图" src="./resources/img/data-model.jpg">
				</p>
			</div>
			<div class="span6">
				<h2>简介</h2>
				<p>
					所有的配置信息以config-group的形式进行组织和管理。 <br>用户基于config-group进行读授权，基于用户角色进行写授权。
					<br>每一个config-group具有一个groupName，全局唯一。 <br>每一条配置具有一个groupId，key，value，其中groupId和key全局唯一。
					<br>配置中心提供全方位、多角度的HA，确保应用的稳定性。<br>配置中心支持事件模型，当配置发生变化时通知应用程序。
				</p>
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
			check_alert();
		});
	</script>
</body>
</html>